package hcyxy.tech.PaxosClient

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class PaxosClient {
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var future: ChannelFuture
    private var boot: Bootstrap

    init {
        val workerGroup = NioEventLoopGroup(1, object : ThreadFactory {
            val index = AtomicInteger(0)
            override fun newThread(r: Runnable): Thread {
                return Thread(r, "Client-Thread${index.incrementAndGet()}")
            }
        })

        boot = Bootstrap()
        boot.group(workerGroup).channel(NioSocketChannel::class.java)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_KEEPALIVE, false)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast()
                }
            })
    }

    fun connect(ip: String, port: Int): Channel {
        future = boot.connect(ip, port)
        if (future.isSuccess) {
            return future.channel()
        } else {
            throw Exception("连接出错")
        }
    }
}