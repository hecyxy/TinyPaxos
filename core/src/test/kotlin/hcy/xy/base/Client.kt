package hcy.xy.base

import hcyxy.tech.core.constants.ProposerEventType
import hcyxy.tech.core.info.SubmitValue
import hcyxy.tech.remoting.client.RemotingClientImpl
import hcyxy.tech.remoting.common.RemotingMsgSerializable
import hcyxy.tech.remoting.config.ClientConfig
import hcyxy.tech.remoting.protocol.ProcessorCode
import hcyxy.tech.remoting.protocol.RemotingMsg

fun main(vararg args: String) {
    val client = RemotingClientImpl(ClientConfig())
    client.start()
    val value =SubmitValue("heihei")
    val body = RemotingMsgSerializable.encode(value)
    val msg = RemotingMsg.createRequest(ProcessorCode.PROPOSER.code, "aaa", ProposerEventType.Submit.index, body)
    val result = client.invokeSync("127.0.0.1:8088", msg, 10000)
    println(result)
}