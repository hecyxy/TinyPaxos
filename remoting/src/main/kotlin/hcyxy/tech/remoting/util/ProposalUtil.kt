package hcyxy.tech.remoting.util

import hcyxy.tech.remoting.entity.*

object ProposalUtil {

    fun generateProposal(
        eventType: EventType?,
        actionType: ActionType?,
        proposalId: Long?,
        message: String?,
        packet: Packet?,
        remotingCode: RemotingCode?
    ): Proposal {
        return Proposal().apply {
            eventType?.let { this.eventType = it }
            actionType?.let { this.actionType = it }
            proposalId?.let { this.proposalId = it }
            message?.let { this.message = it }
            packet?.let { this.packet = it }
            remotingCode?.let { this.remotingCode = it }
        }
    }
}