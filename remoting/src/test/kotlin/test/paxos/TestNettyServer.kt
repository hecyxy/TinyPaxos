package test.paxos

import hcyxy.tech.remoting.client.RemotingClientImpl
import hcyxy.tech.remoting.config.ClientConfig
import hcyxy.tech.remoting.protocol.ActionCode
import hcyxy.tech.remoting.protocol.ProcessorCode
import hcyxy.tech.remoting.protocol.RemotingMsg

class TestNettyServer {

}

fun main() {
    val client = RemotingClientImpl(ClientConfig())
    client.start()
    val msg = RemotingMsg()
    msg.setActionCode(ActionCode.REQUEST.code)
    msg.setProcessorCode(ProcessorCode.DEFAULT.code)
    msg.setMessage("default request2")
    val body = "hello,u".toByteArray()
    msg.setBody(body)
    val resp1 = client.invokeSync("127.0.0.1:8088", msg, 1000)
    println("receive1 $resp1")
    msg.setMessage("default request3")
    val resp2 = client.invokeSync("127.0.0.1:8088", msg, 1000)
    println("receive2 $resp2")
//
    val msg1 = RemotingMsg()
    msg1.setActionCode(ActionCode.REQUEST.code)
    msg1.setProcessorCode(ProcessorCode.DEFAULT.code)
    msg1.setMessage("default request")
    val body1 = "hello,u".toByteArray()
    msg1.setBody(body1)
    val resp3 = client.invokeSync("127.0.0.1:8088", msg1, 1000)
    println("receive2 $resp3")

//    client.invokeAsync(
//        "127.0.0.1:8888",
//        Proposal(EventType.ACCEPTOR, ActionType.REQUEST, 1000, null),
//        3000,
//        ClientCallback()
//    )
}