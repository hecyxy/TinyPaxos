package hcyxy.tech.core.processor

import hcyxy.tech.core.constants.AcceptorEventType
import hcyxy.tech.core.constants.PaxosConfig
import hcyxy.tech.core.constants.ProposerEventType
import hcyxy.tech.remoting.RequestProcessor
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.entity.*
import hcyxy.tech.remoting.util.ProposalUtil
import java.util.concurrent.ArrayBlockingQueue
import kotlin.math.max

class ProposerProcessor(
    private val serverId: Int,
    private val serverList: List<PaxosConfig.ServerNode>,
    private val client: RemotingClient,
    private val acceptor: AcceptorProcessor
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
        val maxLogId = getMaxLogId()
        println("maxId: $maxLogId")
        val packet = ProposalUtil.generatePacket(
            null,
            maxLogId,
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

    /**
     * @Description get max logId
     */
    private fun getMaxLogId(): Long {
        val logIdList = mutableListOf<Long>()
        val packet = ProposalUtil.generatePacket(
            null,
            null,
            AcceptorEventType.MAX_LOG.index,
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
                null,
                packet,
                null
            )
        serverList.forEach {
            if (it.id != serverId) {
                client.invokeSync("${it.host}:${it.port}", proposal, 3000).packet?.let { packet ->
                    logIdList.add(packet.logId)
                }
            }
        }
        return max(logIdList.max() ?: 0, acceptor.getMaxLogId()) + 1
    }
}