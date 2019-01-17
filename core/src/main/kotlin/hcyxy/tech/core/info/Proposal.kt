package hcyxy.tech.core.info

//事件类型 请求/回复  proposalId packet
//data class Proposal(var eventType: EventType, var actionType: ActionType, var proposalId: Long, var packet: Packet?)
enum class InstanceState {
    PREPARE, ACCEPT, DONE
}

class Packet {
    var proposalId: Long = 0
    var logId: Long = 0
    var packetType: Int = 0
    var value: String? = null
    var promiseSet: Set<Long>? = null
    var acceptSet: Set<Long>? = null
    var isSucc: Boolean = false
    var instanceState: InstanceState? = null
    var serverId: Int? = null
}


class Proposal {
    var proposalId: Long = 0
    var logId: Long = 0
    var packetType: Int = 0
    var value: String? = null
    var promiseSet: Set<Long>? = null
    var acceptSet: Set<Long>? = null
    var isSucc: Boolean = false
    var instanceState: InstanceState? = null
    var serverId: Int? = null
}