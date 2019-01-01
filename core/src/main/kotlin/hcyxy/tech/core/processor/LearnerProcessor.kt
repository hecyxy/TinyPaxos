package hcyxy.tech.core.processor

import hcyxy.tech.remoting.RequestProcessor
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.entity.ActionType
import hcyxy.tech.remoting.entity.EventType
import hcyxy.tech.remoting.entity.Proposal
import io.netty.channel.ChannelHandlerContext

class LearnerProcessor : RequestProcessor {
    private var remotingClient: RemotingClient? = null

    constructor(client: RemotingClient?) {
        this.remotingClient = client
    }

    override fun processRequest(ctx: ChannelHandlerContext, proposal: Proposal): Proposal {
        return Proposal(EventType.ACCEPTOR, ActionType.RESPONSE, 20, null)
    }
}