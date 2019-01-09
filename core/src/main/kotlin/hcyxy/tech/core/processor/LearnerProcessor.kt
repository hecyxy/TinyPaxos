package hcyxy.tech.core.processor

import hcyxy.tech.core.constants.PaxosConfig
import hcyxy.tech.remoting.RequestProcessor
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.protocol.RemotingMsg

class LearnerProcessor(
    private val serverId: Int,
    private val serverList: List<PaxosConfig.ServerNode>,
    private val client: RemotingClient
) : RequestProcessor {

    override fun processRequest(msg: RemotingMsg): RemotingMsg {
//        val packetType = proposal.packet?.packetType ?: return proposal
//        when (packetType) {
//            LearnerEventType.LearnRequest.code -> {
//
//            }
//            LearnerEventType.LearnResponse.code -> {
//
//            }
//        }
        return RemotingMsg()
    }
}