package hcyxy.tech.remoting

import hcyxy.tech.remoting.common.FlexibleReleaseSemaphore
import hcyxy.tech.remoting.common.RemotingHelper
import hcyxy.tech.remoting.entity.ActionType
import hcyxy.tech.remoting.entity.Proposal
import hcyxy.tech.remoting.entity.RemotingCode
import hcyxy.tech.remoting.util.ProposalUtil
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong

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
    // 注册的各个RPC处理器
    protected val processorTable = HashMap<Int, Pair<RequestProcessor, ExecutorService>>(
        64
    )
    //允许异步请求
//    this.semaphoreAsync = Semaphore(permitAsync, true)
    private val increment = AtomicLong(0)

    protected fun invokeSyncImpl(channel: Channel, proposal: Proposal, timeout: Long): Proposal {
        try {
            val responseFuture = ResponseFuture(increment.incrementAndGet(), timeout, null, null)
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
                logger.warn("send message timeout")
                ProposalUtil.generateProposal(
                    proposal.eventType,
                    ActionType.RESPONSE,
                    proposal.proposalId,
                    "sync invoke send message timeout",
                    proposal.packet,
                    RemotingCode.TIMEOUT
                )
            } else {
                logger.warn("send message failed")
                ProposalUtil.generateProposal(
                    proposal.eventType,
                    ActionType.RESPONSE,
                    proposal.proposalId,
                    "send message failed",
                    proposal.packet,
                    RemotingCode.SEND_MESSAGE_FAILED
                )
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
                logger.warn("invoke async callback timeout")
            }
            val responseFuture = ResponseFuture(increment.incrementAndGet(), timeout - cost, callback, once)
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
        val processor = this.processorTable[proposal.eventType.index]
        val result = processor?.first?.processRequest(proposal)
        if (result == null) {
            val temp = ProposalUtil.generateProposal(
                proposal.eventType,
                ActionType.RESPONSE,
                proposal.proposalId,
                "unknown processor",
                proposal.packet,
                RemotingCode.UNKNOWN_PROCESSOR
            )
            ctx.writeAndFlush(temp)
        } else {
            ctx.writeAndFlush(result)
        }
    }

    private fun processResponse(ctx: ChannelHandlerContext, proposal: Proposal) {
        val future = responseTable[proposal.proposalId]
        if (future != null) {
            future.putResponse(proposal)
            future.executeCallback()
        } else {
            logger.warn("have received response, but do not find any request, address: ${RemotingHelper.channel2Addr(ctx.channel())}")
        }
    }
}