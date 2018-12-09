package hcyxy.tech.server

import hcyxy.tech.entity.EventType
import hcyxy.tech.entity.Proposal
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.slf4j.LoggerFactory

class ServerHandler(private val map: HashMap<Long, Channel>) : SimpleChannelInboundHandler<Proposal>() {
    private val logger = LoggerFactory.getLogger(ServerHandler::class.java)

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Proposal) {
        println("receive some message ${msg.type} ${msg.proposalId} ${msg.packet}")
        val channel = map[msg.proposalId]
        if (channel != null) {
            println("aaaaa")
            channel.writeAndFlush("send hh ...")
        } else {
            map[msg.proposalId] = ctx.channel()
            println("bbbb")
            ctx.channel().writeAndFlush("send ")
        }
//        processRequest(ctx, msg)
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
