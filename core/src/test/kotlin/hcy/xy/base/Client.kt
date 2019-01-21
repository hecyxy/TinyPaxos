package hcy.xy.base

import hcyxy.tech.core.constants.LearnerEventType
import hcyxy.tech.core.constants.ProposerEventType
import hcyxy.tech.core.info.SubmitValue
import hcyxy.tech.core.info.protocol.AcceptProposal
import hcyxy.tech.core.info.protocol.LearnRequest
import hcyxy.tech.core.info.protocol.LearnResponse
import hcyxy.tech.core.util.getByteArray
import hcyxy.tech.core.util.getObject
import hcyxy.tech.remoting.client.RemotingClientImpl
import hcyxy.tech.remoting.common.RemotingMsgSerializable
import hcyxy.tech.remoting.config.ClientConfig
import hcyxy.tech.remoting.protocol.ProcessorCode
import hcyxy.tech.remoting.protocol.RemotingMsg

fun main(vararg args: String) {
    val client = RemotingClientImpl(ClientConfig())
    client.start()
    val value = SubmitValue("heihei")
    val body = value.getByteArray()
    val begin = System.currentTimeMillis()
    val msg =
        RemotingMsg.createRequest(ProcessorCode.PROPOSER.code, "submit value", ProposerEventType.Submit.index, body)
    val result = client.invokeSync("127.0.0.1:8088", msg, 10000)
    println(System.currentTimeMillis() - begin)
    result.getBody()?.let { println(getObject(AcceptProposal::class.java, it)) }

    val value2 = SubmitValue("heihei2")
    val body2 = value2.getByteArray()
    val msg1 = RemotingMsg.createRequest(ProcessorCode.PROPOSER.code, "aaa", ProposerEventType.Submit.index, body2)
    val result2 = client.invokeSync("127.0.0.1:8088", msg1, 10000)
    result2.getBody()?.let { println(getObject(AcceptProposal::class.java, it)) }

    val request = LearnRequest(2)
    val body3 = request.getByteArray()
    val msg3 = RemotingMsg.createRequest(ProcessorCode.LEARNER.code, "aaa", LearnerEventType.LearnRequest.index, body3)
    val result3 = client.invokeSync("127.0.0.1:8089", msg3, 10000)
    result3.getBody()?.let { println(getObject(LearnResponse::class.java, it)) }
}