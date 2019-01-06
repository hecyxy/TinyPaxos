package hcyxy.tech.core.constants

class PaxosConfig {
    var server: List<ServerNode>? = null
    var serverId: Int? = null
    var bossThreads: Int? = null
    var maxConnection: Int? = null
    var publicThreadNum: Int? = null
    var workerThreads: Int? = null
    var lockTime: Long? = null
    var channelWait: Long? = null
    var permitAsync: Int? = null

    class ServerNode {
        var id: Int? = null
        var host: String? = null
        var port: Int? = null
    }
}
