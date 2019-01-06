package hcy.xy.base

import hcyxy.tech.remoting.client.RemotingClientImpl
import hcyxy.tech.remoting.config.ClientConfig
import hcyxy.tech.remoting.entity.ActionType
import hcyxy.tech.remoting.entity.EventType
import hcyxy.tech.remoting.entity.Packet
import hcyxy.tech.remoting.entity.Proposal

fun main(vararg args: String) {
    val client = RemotingClientImpl(ClientConfig())
    client.start()
//    val proposal = Proposal(EventType.ACCEPTOR, ActionType.REQUEST, 100, null)
    val packet = Packet(0, 0, "hello")
    val proposal = Proposal()
    proposal.eventType = EventType.PROPOSER
    proposal.actionType = ActionType.REQUEST
    proposal.proposalId = 100
    proposal.packet = packet
    val resp = client.invokeSync("127.0.0.1:8088", proposal, 5000)
    println("receive $resp")
}