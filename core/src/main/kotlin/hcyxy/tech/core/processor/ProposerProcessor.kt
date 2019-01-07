package hcyxy.tech.core.processor

import hcyxy.tech.core.constants.AcceptorEventType
import hcyxy.tech.core.constants.PaxosConfig
import hcyxy.tech.core.constants.ProposerEventType
import hcyxy.tech.remoting.RequestProcessor
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.entity.*
import hcyxy.tech.remoting.util.ProposalUtil
import java.util.concurrent.ArrayBlockingQueue

class ProposerProcessor(
    private val serverId: Int,
    private val serverList: List<PaxosConfig.ServerNode>,
    private val client: RemotingClient
) : RequestProcessor {
    //保存客户端达成的值
    private val toSubmitArray = ArrayBlockingQueue<Packet>(1)
    //成功提交
    // 成功提交的状态
    private val alreadySubmitArray = ArrayBlockingQueue<Packet>(1)
    /**
     * @Description 执行prepare流程
     */
    private var proposalId: Long = 0

    /**
     * @Description 获取全局递增提案ID
     */
    private fun renewMaxProposalId() {
        this.proposalId = "$serverId${System.currentTimeMillis()}".toLong()
    }

    fun prepare() {

    }

    override fun processRequest(proposal: Proposal): Proposal {
        val packet = proposal.packet ?: return proposal
        return when (packet.packetType) {
            ProposerEventType.Submit.index -> {
                toSubmitArray.put(packet)
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
        renewMaxProposalId()
        val packet =
            Packet(
                null,
                AcceptorEventType.Prepare.index,
                null,
                HashSet(),
                HashSet(),
                false,
                InstanceState.PREPARE,
                serverId
            )
        val proposal =
            ProposalUtil.generateProposal(
                EventType.ACCEPTOR,
                ActionType.REQUEST,
                this.proposalId,
                null,
                packet,
                null
            )
        serverList.forEach {
            if (it.id != serverId) {
                val result = client.invokeSync("${it.host}:${it.port}", proposal, 2000)
            }
        }
    }
}