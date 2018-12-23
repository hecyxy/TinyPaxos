package hcyxy.tech.remoting.entity

data class Proposal(val type: EventType, val actionType: ActionType, val proposalId: Long, val packet: Packet?)
data class Packet(val logId: Long, val value: String)


