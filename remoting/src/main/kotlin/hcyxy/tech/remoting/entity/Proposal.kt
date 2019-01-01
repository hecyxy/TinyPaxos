package hcyxy.tech.remoting.entity
//事件类型 请求/回复  proposalId packet
data class Proposal(val eventType: EventType, val actionType: ActionType, val proposalId: Long, val packet: Packet?)
data class Packet(val logId: Long, val value: String)