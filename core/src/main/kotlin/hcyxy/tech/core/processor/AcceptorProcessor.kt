package hcyxy.tech.core.processor

import hcyxy.tech.core.constants.AcceptorEventType
import hcyxy.tech.core.constants.DefaultEventType
import hcyxy.tech.core.constants.PaxosConfig
import hcyxy.tech.core.constants.ProposerEventType
import hcyxy.tech.core.info.protocol.MaxLogIdInfo
import hcyxy.tech.core.info.Packet
import hcyxy.tech.remoting.RequestProcessor
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.protocol.ProcessorCode
import hcyxy.tech.remoting.protocol.RemotingMsg
import java.util.*

class AcceptorProcessor(
    private val serverId: Int,
    private val serverList: List<PaxosConfig.ServerNode>,
    private val client: RemotingClient
) : RequestProcessor, AbstractProcessor() {
    //include every request
    private val packetMap = HashMap<Long, Packet>()
    //alread accept value
    private val acceptValue = HashMap<Long, String>()

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
                    "max log id",
                    body.getByteArray()
                )
            }
            else -> {
                createErrorResponse("unknown processor event type")
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