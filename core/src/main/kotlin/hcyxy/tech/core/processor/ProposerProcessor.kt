package hcyxy.tech.core.processor

import hcyxy.tech.core.constants.AcceptorEventType
import hcyxy.tech.core.constants.ProposerEventType
import hcyxy.tech.remoting.RequestProcessor
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.entity.ActionType
import hcyxy.tech.remoting.entity.EventType
import hcyxy.tech.remoting.entity.Packet
import hcyxy.tech.remoting.entity.Proposal
import java.lang.Exception

class ProposerProcessor(private val client: RemotingClient, private val machineId: Int) : RequestProcessor {

    /**
     * @Description 执行prepare流程
     */
    private var proposalId: Long = 0

    /**
     * @Description 获取全局递增提案ID
     */
    fun getMaxProposalId(): Long {
        return "$machineId${System.currentTimeMillis()}".toLong()
    }

    fun prepare() {

    }

    override fun processRequest(proposal: Proposal): Proposal {
        val packet = proposal.packet ?: return Proposal(EventType.LEARNER, ActionType.RESPONSE, 0, null)
        return when (packet.packetType) {
            ProposerEventType.Submit.index -> {
                Proposal(
                    EventType.ACCEPTOR,
                    ActionType.RESPONSE,
                    20,
                    Packet(packet.logId, AcceptorEventType.Prepare.index, "hello")
                )
            }
            ProposerEventType.PrepareResponse.index -> {
                Proposal(EventType.ACCEPTOR, ActionType.RESPONSE, 20, null)
            }
            ProposerEventType.AcceptResponse.index -> {
                Proposal(EventType.ACCEPTOR, ActionType.RESPONSE, 20, null)
            }
            else -> {
                Proposal(EventType.ACCEPTOR, ActionType.RESPONSE, 20, null)
            }
        }
    }

}