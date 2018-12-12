package hcyxy.tech.remoting.common

import io.netty.channel.Channel
import java.net.InetSocketAddress
import java.net.SocketAddress

object RemotingHelper {
    fun string2Addr(addr: String): SocketAddress {
        val temp = addr.split(":")
        return InetSocketAddress(temp[0], temp[1].toInt())
    }

    fun channel2Addr(channel: Channel): String {
        val remote = channel.remoteAddress()
        val addr = remote?.toString() ?: ""
        if (addr.isNotEmpty()) {
            val index = addr.lastIndexOf("/")
            return if (index >= 0) {
                addr.substring(index + 1)
            } else addr
        }
        return ""
    }
}