package test.paxos

import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.client.RemotingClientImpl
import hcyxy.tech.remoting.common.RemotingMsgSerializable
import hcyxy.tech.remoting.entity.EventType
import hcyxy.tech.remoting.entity.Proposal

class TestNettyServer {

}

fun main() {
    val client = RemotingClientImpl()
    client.start()
    val proposal = Proposal(EventType.ACCEPTOR, 100, null)
    val msg = RemotingMsgSerializable.encode(proposal)
    client.invokeSync("127.0.0.1:8888", Proposal(EventType.ACCEPTOR, 100, null), 2000)
}