package hcyxy.tech.remoting

import hcyxy.tech.remoting.common.FlexibleReleaseSemaphore
import hcyxy.tech.remoting.common.RemotingHelper
import hcyxy.tech.remoting.entity.ActionType
import hcyxy.tech.remoting.entity.Proposal
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * @Description server和client的公共抽象方法
 * 这个抽象类还可以缓存请求 启动一个定时任务 扫描缓存的请求  便于管控
 */
abstract class RemotingAbstract {
    private val logger = LoggerFactory.getLogger(RemotingAbstract::class.java)
    // 信号量，异步调用情况会使用，防止本地Netty缓存请求过多
    protected var semaphoreAsync: Semaphore? = null
    //缓存对外所有请求
    private val responseTable: ConcurrentMap<Long, ResponseFuture> = ConcurrentHashMap(256)

    //允许异步请求
//    this.semaphoreAsync = Semaphore(permitAsync, true)

    protected fun invokeSyncImpl(channel: Channel, proposal: Proposal, timeout: Long): Proposal {
        try {
            val responseFuture = ResponseFuture(proposal.proposalId, timeout, null, null)
            this.responseTable[proposal.proposalId] = responseFuture
            channel.writeAndFlush(proposal).addListener { future ->
                if (future.isSuccess) {
                    responseFuture.setSendRequestOk(true)
                    return@addListener
                } else {
                    responseFuture.setSendRequestOk(false)
                }
                this.responseTable.remove(proposal.proposalId)
                responseFuture.setCause(future.cause())
                responseFuture.putResponse(null)
            }
            return responseFuture.waitResponse(timeout) ?: if (responseFuture.isSendRequestOk()) {
                channel.close()
                throw Exception("send request timeout $timeout")
            } else {
                throw Exception("send request failed")
            }
        } finally {
            this.responseTable.remove(proposal.proposalId)
        }
    }

    protected fun invokeAsyncImpl(channel: Channel, proposal: Proposal, timeout: Long, callback: InvokeCallback) {
        val begin = System.currentTimeMillis()
        val acquired = this.semaphoreAsync?.tryAcquire(timeout, TimeUnit.MILLISECONDS)
        if (acquired != null && acquired) {
            val cost = System.currentTimeMillis() - begin
            val once = FlexibleReleaseSemaphore(this.semaphoreAsync, 1)
            if (timeout < cost) {
                once.release()
                throw Exception("invoke async callback timeout")
            }
            val responseFuture = ResponseFuture(proposal.proposalId, timeout - cost, callback, once)
            this.responseTable[proposal.proposalId] = responseFuture
            try {
                channel.writeAndFlush(proposal).addListener { future ->
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
                logger.warn("send request exception", e)
                throw Exception("addr {${RemotingHelper.channel2Addr(channel)}} exception")
            }
        } else {
            if (timeout <= 0) {
                logger.warn("invoke async too fast")
            } else {
                logger.warn("try acquire timeout,waiting threads ${this.semaphoreAsync?.queueLength} semaphore permits: ${this.semaphoreAsync?.availablePermits()}")
                throw Exception("time out")
            }
        }
    }

    fun processReceiveMessage(ctx: ChannelHandlerContext, proposal: Proposal) {
        when (proposal.actionType) {
            ActionType.REQUEST -> {
                processRequest(ctx, proposal)
            }
            ActionType.RESPONSE -> {
                processResponse(ctx, proposal)
            }
        }
    }

    private fun processRequest(ctx: ChannelHandlerContext, proposal: Proposal) {
        val response = Proposal(proposal.type, ActionType.RESPONSE, proposal.proposalId, null)
        ctx.writeAndFlush(response)
    }

    private fun processResponse(ctx: ChannelHandlerContext, proposal: Proposal) {
        val future = responseTable[proposal.proposalId]
        future?.putResponse(proposal)
        future?.executeCallback()
    }

}