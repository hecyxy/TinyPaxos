package hcy.xy.base

import hcyxy.tech.core.constants.ProposerEventType
import hcyxy.tech.core.entity.Packet
import hcyxy.tech.remoting.client.RemotingClientImpl
import hcyxy.tech.remoting.config.ClientConfig

fun main(vararg args: String) {
    val client = RemotingClientImpl(ClientConfig())
    client.start()
    val packet = Packet()
    packet.packetType = ProposerEventType.Submit.index
}