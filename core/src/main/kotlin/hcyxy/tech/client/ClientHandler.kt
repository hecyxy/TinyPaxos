package hcyxy.tech.client

import hcyxy.tech.entity.Proposal
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.slf4j.LoggerFactory

class ClientHandler : SimpleChannelInboundHandler<Proposal>() {
    private val logger = LoggerFactory.getLogger(javaClass)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Proposal) {
        //TODO client收到不同accept如何处理
        logger.info("read message")
    }
}