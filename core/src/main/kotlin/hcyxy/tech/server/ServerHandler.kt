package hcyxy.tech.server

import hcyxy.tech.entity.EventType
import hcyxy.tech.entity.Proposal
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.slf4j.LoggerFactory

class ServerHandler : SimpleChannelInboundHandler<Proposal>() {
    private val logger = LoggerFactory.getLogger(ServerHandler::class.java)

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Proposal) {
        println("receive some message ${msg.type} ${msg.proposalId} ${msg.packet}")
        processRequest(ctx, msg)
    }

    fun processRequest(ctx: ChannelHandlerContext, msg: Proposal) {
        when (msg.type) {
            EventType.PROPOSER -> {
                ctx.writeAndFlush(msg)
            }
            EventType.ACCEPTOR -> {
            }
            EventType.LEARNER -> {
            }
            EventType.LEADER -> {
            }
        }
    }
}
