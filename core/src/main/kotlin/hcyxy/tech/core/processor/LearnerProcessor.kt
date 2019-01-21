package hcyxy.tech.core.processor

import hcyxy.tech.core.constants.AcceptorEventType
import hcyxy.tech.core.constants.LearnerEventType
import hcyxy.tech.core.constants.PaxosConfig
import hcyxy.tech.core.info.protocol.*
import hcyxy.tech.core.util.getByteArray
import hcyxy.tech.core.util.getObject
import hcyxy.tech.core.util.notnull
import hcyxy.tech.remoting.RequestProcessor
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.common.RemotingHelper
import hcyxy.tech.remoting.protocol.ProcessorCode
import hcyxy.tech.remoting.protocol.RemotingMsg

class LearnerProcessor(
    private val serverId: Int,
    private val serverList: List<PaxosConfig.ServerNode>,
    private val client: RemotingClient,
    private val acceptor: AcceptorProcessor
) : RequestProcessor, AbstractProcessor() {

    private val empty = "##$$"
    override fun processRequest(msg: RemotingMsg): RemotingMsg {
        return when (msg.getProcessorEventType()) {
            LearnerEventType.LearnRequest.index -> {
                val remotingBody =
                    msg.getBody() ?: return createErrorResponse(msg.getRequestId(), "learn request empty body")
                val learnRequest = getObject(LearnRequest::class.java, remotingBody)
                onLearnPrepare(learnRequest, msg.getRequestId())
            }
            else -> {
                createErrorResponse(msg.getRequestId(), "wrong learn request processor event type")
            }
        }
    }

    /**
     * @Description 执行learn prepare流程
     */
    private var proposalId: Long = 0

    /**
     * @Description 获取全局递增提案ID
     */
    private fun renewMaxProposalId() {
        this.proposalId = "${System.currentTimeMillis()}$serverId".toLong()
    }

    private fun onLearnPrepare(learnRequest: LearnRequest, requestId: Long): RemotingMsg {
        renewMaxProposalId()
        val prepareRequest = createLearnPaepare(learnRequest.logId, this.proposalId)
        val receiveList = mutableListOf<PrepareResponse>()
        serverList.forEach {
            if (it.id != serverId) {
                val result =
                    client.invokeSync(RemotingHelper.getAddress(it.host, it.port), prepareRequest, 1000).getBody()
                if (result != null) {
                    receiveList.add(getObject(PrepareResponse::class.java, result))
                }
            }
        }

        val acceptList = receiveList.filter { it.accept }
        return if ((acceptList.size + 1) > serverList.size / 2) {
            val valueList = acceptList.filter { it.content != null }.sortedByDescending { it.proposalId }
            if (valueList.isNotEmpty()) {
                //send accept request with response value which has max proposalId
                val value = notnull(valueList.first().content?.value, "system error")
                val acceptRequest = createAcceptRequest(learnRequest.logId, this.proposalId, value)
                onLearnAccept(acceptRequest, requestId)
            } else {
                //send accept request with specialized value
                val acceptRequest = createAcceptRequest(learnRequest.logId, this.proposalId, empty)
                onLearnAccept(acceptRequest, requestId)
            }
        } else {
            createErrorResponse(requestId, "unknown state")
        }
    }

    private fun onLearnAccept(acceptRequest: RemotingMsg, requestId: Long): RemotingMsg {
        val receiveAcceptList = mutableListOf<AcceptResponse>()
        serverList.forEach { node ->
            if (node.id != serverId) {
                val result =
                    client.invokeSync(RemotingHelper.getAddress(node.host, node.port), acceptRequest, 2000).getBody()
                result?.let { receiveAcceptList.add(getObject(AcceptResponse::class.java, it)) }
            }
        }
        val acceptedList = receiveAcceptList.filter { it.accept }
        return if ((acceptedList.size + 1) > serverList.size / 2) {
            val returnBody = notnull(acceptRequest.getBody(), "value is null,system error")
            val value = getObject(AcceptRequest::class.java, returnBody)
            if (acceptor.getAcceptValue()[value.logId] == null) {
                val acceptProposal = AcceptProposal(value.logId, value.proposalId, value.content)
                acceptor.getAcceptValue()[value.logId] = acceptProposal
                acceptor.getSavedValue().add(acceptProposal)
            }
            createResultResponse(requestId, LearnResponse(value.logId, value.content).getByteArray())
        } else {
            createErrorResponse(requestId, "unknown state")
        }
    }

    private fun createLearnPaepare(logId: Long, proposalId: Long): RemotingMsg {
        return RemotingMsg.createRequest(
            ProcessorCode.ACCEPTOR.code, "learn prepare request", AcceptorEventType.Prepare.index,
            PrepareRequest(logId, proposalId).getByteArray()
        )
    }

    /**
     * @Description create request
     */
    private fun createAcceptRequest(logId: Long, proposalId: Long, content: String): RemotingMsg {
        return RemotingMsg.createRequest(
            ProcessorCode.ACCEPTOR.code, "learn accept request", AcceptorEventType.Accept.index,
            AcceptRequest(logId, proposalId, content).getByteArray()
        )
    }

    private fun createResultResponse(requestId: Long, body: ByteArray): RemotingMsg {
        return createOkResponse(requestId, "success get result", body)
    }
}