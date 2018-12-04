package test.paxos

import hcyxy.tech.client.PaxosClient
import hcyxy.tech.entity.EventType
import hcyxy.tech.entity.Proposal
import hcyxy.tech.entity.RemotingMsg
import hcyxy.tech.util.RemotingMsgSerializable

fun main(vararg args: String) {
    val channel = PaxosClient().connect("localhost", 1111)
    val proposal = Proposal(EventType.PROPOSER, 1, null)
    val temp = RemotingMsgSerializable.encode(proposal)
    val remoting = RemotingMsg()
    remoting.setBody(temp)
    channel.writeAndFlush(remoting)
}