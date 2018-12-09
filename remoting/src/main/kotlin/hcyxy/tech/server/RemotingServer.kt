package hcyxy.tech.server

import hcyxy.tech.RemotingService
import io.netty.channel.Channel

interface RemotingServer : RemotingService {
    abstract fun invokeSync(channel: Channel)

    abstract fun invokeAsync(channel: Channel)
}