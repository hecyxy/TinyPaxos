package hcyxy.tech.remoting

import hcyxy.tech.remoting.entity.Proposal
import io.netty.channel.ChannelHandlerContext

interface RequestProcessor {
    fun processRequest(proposal: Proposal): Proposal
}