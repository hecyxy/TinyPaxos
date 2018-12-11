package hcyxy.tech.remoting.server

import hcyxy.tech.remoting.RemotingService
import io.netty.channel.Channel

interface RemotingServer : RemotingService {
    abstract fun invokeSync(channel: Channel)

    abstract fun invokeAsync(channel: Channel)
}