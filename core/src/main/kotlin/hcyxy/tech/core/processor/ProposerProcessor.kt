package hcyxy.tech.core.processor

import hcyxy.tech.core.constants.AcceptorEventType
import hcyxy.tech.core.constants.PaxosConfig
import hcyxy.tech.core.constants.ProposerEventType
import hcyxy.tech.core.info.SubmitValue
import hcyxy.tech.core.info.protocol.*
import hcyxy.tech.core.util.InfoUtil
import hcyxy.tech.core.util.getByteArray
import hcyxy.tech.core.util.getObject
import hcyxy.tech.core.util.notnull
import hcyxy.tech.remoting.RequestProcessor
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.common.RemotingHelper
import hcyxy.tech.remoting.protocol.ProcessorCode
import hcyxy.tech.remoting.protocol.RemotingMsg
import org.slf4j.LoggerFactory
import java.util.concurrent.ArrayBlockingQueue

class ProposerProcessor(
    private val serverId: Int,
    private val serverList: List<PaxosConfig.ServerNode>,
    private val client: RemotingClient,
    private val acceptor: AcceptorProcessor
) : RequestProcessor, AbstractProcessor() {
    private val logger = LoggerFactory.getLogger(javaClass)
    //保存客户端达成的值
    private val toSubmitArray = ArrayBlockingQueue<SubmitValue>(1)
    //成功提交
    // 成功提交的状态
    private val alreadySubmitArray = ArrayBlockingQueue<SubmitValue>(1)
    /**
     * @Description 执行prepare流程
     */
    private var proposalId: Long = 0

    /**
     * @Description 获取全局递增提案ID
     */
    private fun renewMaxProposalId() {
        this.proposalId = "${System.currentTimeMillis()}$serverId".toLong()
    }

    override fun processRequest(msg: RemotingMsg): RemotingMsg {
        return when (msg.getProcessorEventType()) {
            ProposerEventType.Submit.index -> {
                val remotingBody = msg.getBody() ?: return RemotingMsg()
                val value = InfoUtil.byte2SubmitValue(remotingBody) ?: return RemotingMsg()
                val submitValue = SubmitValue(value.content)
                toSubmitArray.put(submitValue)
                sendPrepare(msg.getRequestId())
            }
            else -> {
                createErrorResponse(msg.getRequestId(), "unknown processor event type")
            }
        }
    }

    /**
     * @Description 发送prepare
     */
    private fun sendPrepare(requestId: Long): RemotingMsg {
        val acceptRequest = executeProcess()
        val receiveAcceptList = mutableListOf<AcceptResponse>()
        //accept
        serverList.forEach { node ->
            if (node.id != serverId) {
                val result =
                    client.invokeSync(RemotingHelper.getAddress(node.host, node.port), acceptRequest, 2000).getBody()
                result?.let { receiveAcceptList.add(getObject(AcceptResponse::class.java, it)) }
            }
        }
        println("receive accept list $receiveAcceptList")
        val successList = receiveAcceptList.filter { it.accept }
        return if ((successList.size + 1) > serverList.size / 2) {
            val submitBoby = notnull(acceptRequest.getBody(), "protocol error")
            val value = getObject(AcceptRequest::class.java, submitBoby)
            println("value $value")
            val acceptProposal = AcceptProposal(value.logId, value.proposalId, value.content)
            acceptor.getAcceptValue()[value.logId] = acceptProposal
            acceptor.getSavedValue().add(acceptProposal)
            toSubmitArray.poll()
            createOkResponse(requestId, "success submit", acceptProposal.getByteArray())
        } else {
            sendPrepare(requestId)
        }
    }

    private fun executeProcess(): RemotingMsg {
        val maxLogId = getMaxLogId()
        logger.info("get maxLogId $maxLogId")
        renewMaxProposalId()
        val prepareRequest = createPrepareRequest(maxLogId, this.proposalId)
        val receiveList = mutableListOf<PrepareResponse>()
        //prepare
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
            val valueList = acceptList.filter { it.content != null }.sortedBy { it.proposalId }
            if (valueList.isNotEmpty()) {
                //send accept request with response value which has max proposalId
                executeProcess()
            } else {
                //send accept request with specialized value
                createAcceptRequest(maxLogId, this.proposalId, toSubmitArray.peek().content)
            }
        } else {
            //renew renew proposalId,try again
            executeProcess()
        }
    }

    /**
     * @Description get max logId
     */
    private fun getMaxLogId(): Long {
        val logIdList = mutableListOf<Long>()
        val maxLogIdRequest = createMaxLogIdRequest()
        serverList.forEach {
            if (it.id != serverId) {
                val returnBody =
                    client.invokeSync(RemotingHelper.getAddress(it.host, it.port), maxLogIdRequest, 1000).getBody()
                returnBody?.let { logIdList.add(getObject(MaxLogIdInfo::class.java, returnBody).logID) }
            }
        }
        return Math.max(logIdList.max() ?: 0, acceptor.getMaxLogId()) + 1
    }

    /**
     * @Description create max logId request
     */
    private fun createMaxLogIdRequest(): RemotingMsg {
        return RemotingMsg.createRequest(
            ProcessorCode.ACCEPTOR.code,
            "get max log id",
            AcceptorEventType.MAX_LOG.index,
            null
        )
    }

    /**
     * @Description create prepare request
     */
    private fun createPrepareRequest(logId: Long, proposalId: Long): RemotingMsg {
        return RemotingMsg.createRequest(
            ProcessorCode.ACCEPTOR.code, "prepare request", AcceptorEventType.Prepare.index,
            PrepareRequest(logId, proposalId).getByteArray()
        )
    }

    /**
     * @Description create request
     */
    private fun createAcceptRequest(logId: Long, proposalId: Long, content: String): RemotingMsg {
        return RemotingMsg.createRequest(
            ProcessorCode.ACCEPTOR.code, "accept request", AcceptorEventType.Accept.index,
            AcceptRequest(logId, proposalId, content).getByteArray()
        )
    }

}