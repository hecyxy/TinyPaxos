package hcyxy.tech.core.processor

import hcyxy.tech.core.constants.LearnerEventType
import hcyxy.tech.core.constants.PaxosConfig
import hcyxy.tech.remoting.RequestProcessor
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.entity.ActionType
import hcyxy.tech.remoting.entity.EventType
import hcyxy.tech.remoting.entity.Proposal
import io.netty.channel.ChannelHandlerContext

class LearnerProcessor(
    private val serverId: Int,
    private val serverList: List<PaxosConfig.ServerNode>,
    private val client: RemotingClient
) : RequestProcessor {

    override fun processRequest(proposal: Proposal): Proposal {
        val packetType = proposal.packet?.packetType ?: return proposal
        when (packetType) {
            LearnerEventType.LearnRequest.index -> {

            }
            LearnerEventType.LearnResponse.index -> {

            }
        }
        return proposal
    }
}