package hcyxy.tech.core.processor

import hcyxy.tech.core.constants.AcceptorEventType
import hcyxy.tech.core.constants.PaxosConfig
import hcyxy.tech.core.constants.ProposerEventType
import hcyxy.tech.remoting.RequestProcessor
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.entity.ActionType
import hcyxy.tech.remoting.entity.EventType
import hcyxy.tech.remoting.entity.Packet
import hcyxy.tech.remoting.entity.Proposal

class ProposerProcessor(
    private val serverId: Int,
    private val serverList: List<PaxosConfig.ServerNode>,
    private val client: RemotingClient
) : RequestProcessor {

    /**
     * @Description 执行prepare流程
     */
    private var proposalId: Long = 0

    /**
     * @Description 获取全局递增提案ID
     */
    fun getMaxProposalId(): Long {
        return "$serverId${System.currentTimeMillis()}".toLong()
    }

    fun prepare() {

    }

    override fun processRequest(proposal: Proposal): Proposal {
        val proposal = Proposal()
        proposal.eventType = EventType.ACCEPTOR
        proposal.actionType = ActionType.REQUEST
        proposal.proposalId = 100
        val packet = proposal.packet ?: return proposal
        return when (packet.packetType) {
            ProposerEventType.Submit.index -> {
                sendPrepare()
                proposal
            }
            ProposerEventType.PrepareResponse.index -> {
               proposal
            }
            ProposerEventType.AcceptResponse.index -> {
                proposal
            }
            else -> {
                proposal
            }
        }
    }

    /**
     * @Description 发送prepare
     */
    private fun sendPrepare() {
        val proposal = Proposal()
        proposal.eventType = EventType.ACCEPTOR
        proposal.actionType = ActionType.REQUEST
        proposal.proposalId = 100
        serverList.forEach {
            if (it.id != serverId)
                client.invokeSync("${it.host}:${it.port}", proposal, 2000)
        }
    }
}