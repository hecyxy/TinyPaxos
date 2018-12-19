package test.paxos

import hcyxy.tech.remoting.server.RemotingServerImpl

fun main(vararg args: String) {
    val server = RemotingServerImpl()
    server.start()
}