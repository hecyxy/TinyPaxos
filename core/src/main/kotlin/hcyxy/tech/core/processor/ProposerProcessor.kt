package hcyxy.tech.core.processor

import hcyxy.tech.core.constants.AcceptorEventType
import hcyxy.tech.core.constants.PaxosConfig
import hcyxy.tech.core.constants.ProposerEventType
import hcyxy.tech.core.info.Packet
import hcyxy.tech.core.info.SubmitValue
import hcyxy.tech.core.info.protocol.MaxLogIdInfo
import hcyxy.tech.core.util.InfoUtil
import hcyxy.tech.remoting.RequestProcessor
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.protocol.ActionCode
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

    fun prepare() {

    }

    override fun processRequest(msg: RemotingMsg): RemotingMsg {
        return when (msg.getProcessorEventType()) {
            ProposerEventType.Submit.index -> {
                val remotingBody = msg.getBody() ?: return RemotingMsg()
                val value = InfoUtil.byte2SubmitValue(remotingBody) ?: return RemotingMsg()
                val submitValue = SubmitValue(value.content)
                toSubmitArray.put(submitValue)
                sendPrepare()
                createOkResponse(msg.getRequestId(), "success")
            }
            ProposerEventType.PrepareResponse.index -> {
                RemotingMsg()
            }
            ProposerEventType.AcceptResponse.index -> {
                RemotingMsg()
            }
            else -> {
                createErrorResponse(msg.getRequestId(), "unknown processor event type")
            }
        }
    }

    /**
     * @Description 发送prepare
     */
    private fun sendPrepare() {
        val maxLogId = getMaxLogId()
        logger.info("get maxLogId $maxLogId")
        renewMaxProposalId()
//        val prepareRequest = createPrepareRequest()
//        serverList.forEach {
//            if (it.id != serverId) {
//                val result = client.invokeSync("${it.host}:${it.port}", prepareRequest, 2000)
//            }
//        }
    }

    /**
     * @Description get max logId
     */
    private fun getMaxLogId(): Long {
        val logIdList = mutableListOf<Long>()
        val maxLogIdRequest = createMaxLogIdRequest()
        serverList.forEach {
            if (it.id != serverId) {
                val returnBody = client.invokeSync("${it.host}:${it.port}", maxLogIdRequest, 3000).getBody()
                returnBody?.let { logIdList.add(MaxLogIdInfo.getObject(returnBody).getLogId()) }
            }
        }
        return Math.max(logIdList.max() ?: 0, acceptor.getMaxLogId()) + 1
    }

    /**
     * @Descrition create max logId request
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
    private fun createPrepareRequest(): RemotingMsg {
        return RemotingMsg.createRequest(
            ProcessorCode.ACCEPTOR.code, "prepare request", AcceptorEventType.Prepare.index,
            null
        )
    }

}