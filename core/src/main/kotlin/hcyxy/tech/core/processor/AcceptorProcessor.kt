package hcyxy.tech.core.processor

import hcyxy.tech.core.constants.AcceptorEventType
import hcyxy.tech.core.constants.PaxosConfig
import hcyxy.tech.core.info.Packet
import hcyxy.tech.core.info.protocol.MaxLogIdInfo
import hcyxy.tech.remoting.RequestProcessor
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.protocol.RemotingMsg
import java.util.*

class AcceptorProcessor(
    private val serverId: Int,
    private val serverList: List<PaxosConfig.ServerNode>,
    private val client: RemotingClient
) : RequestProcessor, AbstractProcessor() {
    //include every request
    private val packetMap = HashMap<Long, Packet>()
    //alreay accept value
    private val acceptValue = HashMap<Long, String>()
    //every logId map logId:proposalId
    private val logMap = HashMap<Long, Long>()

    override fun processRequest(msg: RemotingMsg): RemotingMsg {
        return when (msg.getProcessorEventType()) {
            AcceptorEventType.Prepare.index -> {
                RemotingMsg()
            }
            AcceptorEventType.Accept.index -> {
                RemotingMsg()
            }
            AcceptorEventType.MAX_LOG.index -> {
                val maxLogId = getMaxLogId()
                val body = MaxLogIdInfo()
                body.setLogId(maxLogId)
                RemotingMsg.createResponse(
                    msg.getRequestId(),
                    "max log id",
                    body.getByteArray()
                )
            }
            else -> {
                createErrorResponse(msg.getRequestId(), "unknown processor event type")
            }
        }
    }


    fun getMaxLogId(): Long {
        return acceptValue.map { it.key }.max() ?: 0
    }
}