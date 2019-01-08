package hcyxy.tech.core.processor

import hcyxy.tech.core.constants.AcceptorEventType
import hcyxy.tech.core.constants.PaxosConfig
import hcyxy.tech.core.constants.ProposerEventType
import hcyxy.tech.remoting.RequestProcessor
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.entity.*
import hcyxy.tech.remoting.util.ProposalUtil
import io.netty.channel.ChannelHandlerContext
import java.util.HashMap

class AcceptorProcessor(
    private val serverId: Int,
    private val serverList: List<PaxosConfig.ServerNode>,
    private val client: RemotingClient
) : RequestProcessor {
    //include every request
    private val packetMap = HashMap<Long, Packet>()
    //alread accept value
    private val acceptValue = HashMap<Long, String>()

    override fun processRequest(proposal: Proposal): Proposal {
        val packet = proposal.packet
        val packetType = packet?.packetType ?: return proposal
        return when (packetType) {
            AcceptorEventType.Prepare.index -> {
                prepare(packet)
                proposal
            }
            AcceptorEventType.Accept.index -> {
                proposal
            }
            AcceptorEventType.MAX_LOG.index -> {
                val meta = ProposalUtil.generatePacket(
                    null,
                    getMaxLogId(),
                    EventType.PROPOSER.index,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
                ProposalUtil.generateProposal(EventType.PROPOSER, ActionType.RESPONSE, null, null, null)
            }
            else -> {
                ProposalUtil.generateProposal(
                    EventType.DEFAULT,
                    ActionType.RESPONSE,
                    "unknown packet",
                    null,
                    RemotingCode.UNKNOWN_PACKET
                )
            }
        }
    }

    private fun prepare(packet: Packet) {
        if (packetMap.containsKey(packet.proposalId)) {
            val prePacket = packetMap[packet.proposalId]
            if (prePacket != null && prePacket.logId > packet.logId) {

            } else {

            }
        } else {
            packetMap[packet.logId] = packet
        }
    }

    fun getMaxLogId(): Long {
        return acceptValue.map { it.key }.max() ?: 0
    }
}