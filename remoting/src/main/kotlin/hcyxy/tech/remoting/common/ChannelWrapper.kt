package hcyxy.tech.remoting.common

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture

class ChannelWrapper(private val channelFuture: ChannelFuture) {

    fun isOK(): Boolean {
        return this.channelFuture.channel() != null && this.channelFuture.channel().isActive
    }

    fun isWriteable(): Boolean {
        return this.channelFuture.channel().isWritable
    }

    fun getChannel(): Channel {
        return this.channelFuture.channel()
    }

    fun getChannelFuture(): ChannelFuture {
        return channelFuture
    }

}