package hcyxy.tech.remoting.util

import hcyxy.tech.remoting.entity.*

object ProposalUtil {

    fun generateProposal(
        eventType: EventType?,
        actionType: ActionType?,
        message: String?,
        packet: Packet?,
        remotingCode: RemotingCode?
    ): Proposal {
        return Proposal().apply {
            eventType?.let { this.eventType = it }
            actionType?.let { this.actionType = it }
            message?.let { this.message = it }
            packet?.let { this.packet = it }
            remotingCode?.let { this.remotingCode = it }
        }
    }

    fun generatePacket(
        proposalId: Long?,
        logId: Long?,
        packetType: Int?,
        value: String?,
        promiseSet: Set<Long>?,
        acceptSet: Set<Long>?,
        isSucc: Boolean?,
        instanceState: InstanceState?,
        serverId: Int?
    ): Packet {
        return Packet().apply {
            proposalId?.let { this.proposalId = it }
            logId?.let { this.logId = it }
            packetType?.let { this.packetType = it }
            value?.let { this.value = it }
            promiseSet?.let { this.promiseSet = it }
            acceptSet?.let { this.acceptSet = it }
            isSucc?.let { this.isSucc = it }
            instanceState?.let { this.instanceState = it }
            serverId?.let { this.serverId = it }
        }
    }
}