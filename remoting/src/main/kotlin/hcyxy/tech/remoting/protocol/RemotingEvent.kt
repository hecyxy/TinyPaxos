package hcyxy.tech.remoting.protocol

import io.netty.channel.Channel

data class RemotingEvent(val type: RemotingEventType, val remoteAddr: String, val channel: Channel)
