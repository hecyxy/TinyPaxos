package hcyxy.tech.remoting

import io.netty.channel.Channel

interface ChannelEventListener {
    abstract fun onChannelConnect(remoteAddr: String, channel: Channel)
    
    abstract fun onChannelClose(remoteAddr: String, channel: Channel)

    abstract fun onChannelIdle(remoteAddr: String, channel: Channel)
}