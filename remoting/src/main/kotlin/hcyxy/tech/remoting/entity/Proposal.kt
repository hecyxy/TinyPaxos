package hcyxy.tech.remoting.entity

//事件类型 请求/回复  proposalId packet
//data class Proposal(var eventType: EventType, var actionType: ActionType, var proposalId: Long, var packet: Packet?)
enum class InstanceState {
    PREPARE, ACCEPT, DONE
}

data class Packet(
    var logId: Long?,
    var packetType: Int,
    var value: String?,
    var promiseSet: Set<Long>?,
    var acceptSet: Set<Long>?,
    var isSucc: Boolean?,
    var instanceState: InstanceState?,
    var serverId: Int?
)

class Proposal {
    var eventType: EventType = EventType.DEFAULT
    var actionType: ActionType = ActionType.REQUEST
    var proposalId: Long = 0
    var message: String = ""
    var packet: Packet? = null
    var remotingCode: RemotingCode? = RemotingCode.NORMAL
}