package hcyxy.tech.entity

data class Proposal(val type: EventType, val proposalId: Long, val packet: Packet?)
data class Packet(val logId: Long, val value: String)

