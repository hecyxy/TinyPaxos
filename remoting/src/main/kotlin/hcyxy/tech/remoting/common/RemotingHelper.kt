package hcyxy.tech.remoting.common

import io.netty.channel.Channel
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.SocketAddress

object RemotingHelper {
    private val logger = LoggerFactory.getLogger(RemotingHelper::class.java)
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

    fun closeChannel(channel: Channel) {
        val remoteAddr = channel2Addr(channel)
        channel.close().addListener { future ->
            logger.info("close the channel. address:{}, result:{}", remoteAddr, future.isSuccess)
        }
    }

    fun getAddress(host: String?, port: Int?): String {
        return "$host:$port"
    }
}