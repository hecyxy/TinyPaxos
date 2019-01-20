package hcyxy.tech.core.processor

import hcyxy.tech.core.constants.AcceptorEventType
import hcyxy.tech.core.constants.PaxosConfig
import hcyxy.tech.core.info.protocol.*
import hcyxy.tech.core.util.getByteArray
import hcyxy.tech.core.util.getObject
import hcyxy.tech.remoting.RequestProcessor
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.protocol.RemotingMsg
import java.util.*

class AcceptorProcessor(
    private val serverId: Int,
    private val serverList: List<PaxosConfig.ServerNode>,
    private val client: RemotingClient
) : RequestProcessor, AbstractProcessor() {
    //simulate save the consistent value
    private val savedValue = ArrayList<AcceptProposal>()
    //prepareMap logId:proposalId
    private val prepareMap = HashMap<Long, Long>()
    //acceptMap logId:proposal
    private val acceptValue = HashMap<Long, AcceptProposal>()
    //every logId map logId:proposalId
    private val logMap = HashMap<Long, Long>()


    override fun processRequest(msg: RemotingMsg): RemotingMsg {
        return when (msg.getProcessorEventType()) {
            AcceptorEventType.Prepare.index -> {
                val msgBody = msg.getBody() ?: return createErrorResponse(msg.getRequestId(), "empty proposal")
                val body = getObject(PrepareRequest::class.java, msgBody)
                onPrepare(msg.getRequestId(), body)
            }
            AcceptorEventType.Accept.index -> {
                onAccept(msg)
            }
            AcceptorEventType.MAX_LOG.index -> {
                val maxLogId = getMaxLogId()
                val body = MaxLogIdInfo(maxLogId)
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

    /**
     * @Description get max logId
     */
    fun getMaxLogId(): Long {
        return acceptValue.map { it.key }.max() ?: 0
    }

    /**
     * @Description handle the prepare request
     */
    private fun onPrepare(requestId: Long, proposal: PrepareRequest): RemotingMsg {
        val prepareProposalId = prepareMap[proposal.logId] ?: 0
        val acceptProposalId = acceptValue[proposal.logId]?.logId ?: 0
        return if (proposal.proposalId <= prepareProposalId || proposal.proposalId < acceptProposalId) {
            val body = PrepareResponse(proposal.logId, proposal.proposalId, false, null).getByteArray()
            // promise not accept proposalId less than current proposalId
            if (proposal.proposalId > acceptProposalId) {
                logMap[proposal.logId] = proposal.proposalId
            }
            createPrepareResponse(requestId, body)
        } else {
            // promise not accept proposalId less than current proposalId
            logMap[proposal.logId] = proposal.proposalId
            prepareMap[proposal.logId] = proposal.proposalId
            val body = if (acceptProposalId > 0) {
                //ignore the operation of saving the current proposalId to disk
                PrepareResponse(proposal.logId, proposal.proposalId, true, acceptValue[proposal.logId]).getByteArray()
            } else {
                PrepareResponse(proposal.logId, proposal.proposalId, true, null).getByteArray()
            }
            createPrepareResponse(requestId, body)
        }
    }

    /**
     * @Description create prepare response
     */
    private fun createPrepareResponse(requestId: Long, body: ByteArray?): RemotingMsg {
        return RemotingMsg.createResponse(requestId, "prepare response", body)
    }

    /**
     * @Description handle accept request
     */
    private fun onAccept(remotingMsg: RemotingMsg): RemotingMsg {
        val msgBody =
            remotingMsg.getBody() ?: return createErrorResponse(remotingMsg.getRequestId(), "empty accept request")
        val acceptRequest = getObject(AcceptRequest::class.java, msgBody)
        val currentLogId = acceptRequest.logId
        val maxProposalId =
            maxOf(logMap[currentLogId] ?: 0, prepareMap[currentLogId] ?: 0, acceptValue[currentLogId]?.logId ?: 0)
        return if (acceptRequest.proposalId >= maxProposalId) {
            //save the proposal to risk(simulation)
            val acceptProposal = AcceptProposal(acceptRequest.logId, acceptRequest.proposalId, acceptRequest.content)
            savedValue.add(acceptProposal)
            acceptValue[acceptRequest.logId] = acceptProposal
            createAcceptResponse(remotingMsg.getRequestId(), AcceptResponse(true).getByteArray())
        } else {
            //accept failed
            createAcceptResponse(remotingMsg.getRequestId(), AcceptResponse(false).getByteArray())
        }
    }

    /**
     * @Description create accept response
     */
    private fun createAcceptResponse(requestId: Long, body: ByteArray?): RemotingMsg {
        return RemotingMsg.createResponse(requestId, "accept response", body)
    }

    fun getAcceptValue() = this.acceptValue

    fun getSavedValue() = this.savedValue
}