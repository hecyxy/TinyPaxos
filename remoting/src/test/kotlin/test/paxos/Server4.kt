package test.paxos

import hcyxy.tech.remoting.config.ServerConfig
import hcyxy.tech.remoting.server.RemotingServerImpl

fun main(vararg args: String) {
    val sererConfig = ServerConfig()
    sererConfig.port = 11111
    val server = RemotingServerImpl(sererConfig)
    server.start()
}