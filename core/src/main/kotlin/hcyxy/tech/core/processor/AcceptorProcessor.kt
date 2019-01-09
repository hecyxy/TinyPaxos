package hcyxy.tech.core.processor

import hcyxy.tech.core.constants.PaxosConfig
import hcyxy.tech.core.entity.Packet
import hcyxy.tech.remoting.RequestProcessor
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.protocol.RemotingMsg
import java.util.*

class AcceptorProcessor(
    private val serverId: Int,
    private val serverList: List<PaxosConfig.ServerNode>,
    private val client: RemotingClient
) : RequestProcessor {
    //include every request
    private val packetMap = HashMap<Long, Packet>()
    //alread accept value
    private val acceptValue = HashMap<Long, String>()

    override fun processRequest(msg: RemotingMsg): RemotingMsg {
//        val packet = proposal.packet
//        val packetType = packet?.packetType ?: return proposal
//        return when (packetType) {
//            AcceptorEventType.Prepare.code -> {
//                prepare(packet)
//                proposal
//            }
//            AcceptorEventType.Accept.code -> {
//                proposal
//            }
//            AcceptorEventType.MAX_LOG.code -> {
//                val meta = ProposalUtil.generatePacket(
//                    null,
//                    getMaxLogId(),
//                    EventType.PROPOSER.code,
//                    null,
//                    null,
//                    null,
//                    null,
//                    null,
//                    null
//                )
//                ProposalUtil.generateProposal(EventType.PROPOSER, ActionType.RESPONSE, null, null, null)
//            }
//            else -> {
//                ProposalUtil.generateProposal(
//                    EventType.DEFAULT,
//                    ActionType.RESPONSE,
//                    "unknown packet",
//                    null,
//                    RemotingCode.UNKNOWN_PACKET
//                )
//            }
//        }
        return RemotingMsg()
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