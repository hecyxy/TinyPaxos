package hcyxy.tech.remoting

import hcyxy.tech.remoting.common.FlexibleReleaseSemaphore
import hcyxy.tech.remoting.common.RemotingHelper
import hcyxy.tech.remoting.protocol.ActionCode
import hcyxy.tech.remoting.protocol.RemotingMsg
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.*

/**
 * @Description server和client的公共抽象方法
 * 这个抽象类还可以缓存请求 启动一个定时任务 扫描缓存的请求  便于管控
 */
abstract class RemotingAbstract//允许异步请求
//    this.semaphoreAsync = Semaphore(permitAsync, true)
    (permitsAsync: Int, permitOnce: Int) {
    private val logger = LoggerFactory.getLogger(RemotingAbstract::class.java)
    // 信号量，异步调用情况会使用，防止本地Netty缓存请求过多
    protected var semaphoreAsync: Semaphore? = null
    //invoke once
    protected var semaphoreOnce: Semaphore? = null
    //缓存对外所有请求
    private val responseTable: ConcurrentMap<Long, ResponseFuture> = ConcurrentHashMap(256)
    // 注册的各个RPC处理器
    protected val processorTable = HashMap<Int, Pair<RequestProcessor, ExecutorService>>(
        64
    )

    init {
        this.semaphoreAsync = Semaphore(permitsAsync, true)
        this.semaphoreOnce = Semaphore(permitOnce, true)
    }

    protected fun invokeSyncImpl(channel: Channel, msg: RemotingMsg, timeout: Long): RemotingMsg {
        val requestId = msg.getRequestId()
        try {
            val responseFuture = ResponseFuture(requestId, timeout, null, null)
            this.responseTable[requestId] = responseFuture
            channel.writeAndFlush(msg).addListener { future ->
                if (future.isSuccess) {
                    responseFuture.setSendRequestOk(true)
                    return@addListener
                } else {
                    responseFuture.setSendRequestOk(false)
                }
                this.responseTable.remove(msg.getRequestId())
                responseFuture.setCause(future.cause())
                responseFuture.putResponse(null)
            }
            return responseFuture.waitResponse(timeout) ?: if (responseFuture.isSendRequestOk()) {
                channel.close()
                logger.warn("send message timeout")
                RemotingMsg.createResponse(requestId, "send message timeout", null)
            } else {
                logger.warn("write message failed")
                RemotingMsg.createResponse(requestId, "write message failed", null)
            }
        } finally {
            this.responseTable.remove(requestId)
        }
    }

    protected fun invokeAsyncImpl(channel: Channel, msg: RemotingMsg, timeout: Long, callback: InvokeCallback) {
        val begin = System.currentTimeMillis()
        val acquired = this.semaphoreAsync?.tryAcquire(timeout, TimeUnit.MILLISECONDS)
        val requestId = msg.getRequestId()
        if (acquired != null && acquired) {
            val cost = System.currentTimeMillis() - begin
            val once = FlexibleReleaseSemaphore(this.semaphoreAsync, 1)
            if (timeout < cost) {
                once.release()
                logger.warn("invoke async callback timeout")
            }
            val responseFuture = ResponseFuture(requestId, timeout - cost, callback, once)
            this.responseTable[requestId] = responseFuture
            try {
                channel.writeAndFlush(msg).addListener { future ->
                    if (future.isSuccess) {
                        responseFuture.setSendRequestOk(true)
                        return@addListener
                    } else {
                        responseFuture.setSendRequestOk(false)
                    }
                    try {
                        responseFuture.executeCallback()
                    } catch (e: Exception) {
                        logger.error("execute callback exception", e)
                    } finally {
                        responseFuture.release()
                    }
                    logger.warn("send request failed")
                }
            } catch (e: Exception) {
                responseFuture.release()
                logger.warn("address {${RemotingHelper.channel2Addr(channel)}} exception ，send request exception", e)
            }
        } else {
            if (timeout <= 0) {
                logger.warn("invoke async too fast")
            } else {
                logger.warn("try acquire timeout,waiting threads ${this.semaphoreAsync?.queueLength} semaphore permits: ${this.semaphoreAsync?.availablePermits()}")
            }
        }
    }

    fun invokeOnceImpl(channel: Channel, msg: RemotingMsg, timeout: Long) {
        val acquired = this.semaphoreAsync?.tryAcquire(timeout, TimeUnit.MILLISECONDS)
        if (acquired != null && acquired) {
            val once = FlexibleReleaseSemaphore(this.semaphoreOnce, 1)
            try {
                channel.writeAndFlush(msg).addListener { future ->
                    once.release()
                    if (!future.isSuccess) {
                        logger.warn("send a once request to ip ${channel.remoteAddress()} failed")
                    }

                }
            } catch (e: Exception) {
                once.release()
                logger.warn("write a request  to channel < ${channel.remoteAddress()} failed")
            }
        } else {
            if (timeout <= 0) {
                throw Exception("wrong params")
            } else {
                val errorInfo =
                    "invoke once,tryAcquire semaphore timeout ${timeout} waiting threads: ${this.semaphoreAsync?.queueLength} available: ${this.semaphoreAsync?.availablePermits()}"
                logger.warn(errorInfo)
                channel.close()
                throw Exception(errorInfo)
            }
        }
    }

    fun processReceiveMessage(ctx: ChannelHandlerContext, msg: RemotingMsg) {
        when (msg.getActionCode()) {
            ActionCode.REQUEST.code -> {
                processRequest(ctx, msg)
            }
            ActionCode.RESPONSE.code -> {
                processResponse(ctx, msg)
            }
        }
    }

    private fun processRequest(ctx: ChannelHandlerContext, msg: RemotingMsg) {
        val processor = this.processorTable[msg.getProcessorCode()]
        val result = processor?.first?.processRequest(msg)
        val requestId = msg.getRequestId()
        if (result == null) {
            val meta = RemotingMsg()
            meta.setRequestId(requestId)
            meta.setActionCode(ActionCode.RESPONSE.code)
            meta.setMessage(msg.getMessage())
            ctx.writeAndFlush(meta)
        } else {
            ctx.writeAndFlush(result)
        }
    }

    private fun processResponse(ctx: ChannelHandlerContext, msg: RemotingMsg) {
        val requestId = msg.getRequestId()
        val future = responseTable[requestId]
        if (future != null) {
            future.putResponse(msg)
            future.executeCallback()
            this.responseTable.remove(requestId)
        } else {
            logger.warn("have received response, but do not find any request, address: ${RemotingHelper.channel2Addr(ctx.channel())}")
        }
    }
}