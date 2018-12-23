package test.paxos

import hcyxy.tech.remoting.client.RemotingClientImpl
import hcyxy.tech.remoting.common.RemotingMsgSerializable
import hcyxy.tech.remoting.entity.ActionType
import hcyxy.tech.remoting.entity.EventType
import hcyxy.tech.remoting.entity.Proposal

class TestNettyServer {

}

fun main() {
    val client = RemotingClientImpl()
    client.start()
    val proposal = Proposal(EventType.ACCEPTOR, ActionType.REQUEST, 100, null)
    val msg = RemotingMsgSerializable.encode(proposal)
    val resp = client.invokeSync("127.0.0.1:8888", Proposal(EventType.ACCEPTOR, ActionType.REQUEST, 100, null), 1000)
    println("receive $resp")

    client.invokeAsync(
        "127.0.0.1:8888",
        Proposal(EventType.ACCEPTOR, ActionType.REQUEST, 1000, null),
        3000,
        ClientCallback()
    )
}