package test.paxos

import hcyxy.tech.client.PaxosClient
import hcyxy.tech.remoting.entity.EventType
import hcyxy.tech.remoting.entity.Proposal
import hcyxy.tech.remoting.entity.RemotingMsg
import hcyxy.tech.remoting.common.RemotingMsgSerializable

fun main(vararg args: String) {
    val channel = PaxosClient().connect("localhost", 11111)
    val proposal = Proposal(EventType.PROPOSER, 1, null)
    val temp = RemotingMsgSerializable.encode(proposal)
    val remoting = RemotingMsg()
    remoting.setBody(temp)
    channel.writeAndFlush(remoting)
    Thread.sleep(2000)
    channel.writeAndFlush(remoting)
}