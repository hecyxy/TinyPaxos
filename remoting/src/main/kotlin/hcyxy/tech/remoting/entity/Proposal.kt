package hcyxy.tech.remoting.entity

//事件类型 请求/回复  proposalId packet
//data class Proposal(var eventType: EventType, var actionType: ActionType, var proposalId: Long, var packet: Packet?)
data class Packet(var logId: Long, var packetType: Int, var value: String)

class Proposal {
    var eventType: EventType = EventType.DEFAULT
    var actionType: ActionType = ActionType.REQUEST
    var proposalId: Long = 0
    var message: String = ""
    var packet: Packet? = null
}