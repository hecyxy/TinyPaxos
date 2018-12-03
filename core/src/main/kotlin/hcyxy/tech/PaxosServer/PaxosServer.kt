package hcyxy.tech.PaxosServer

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.net.InetSocketAddress
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class PaxosServer {
    private val server = ServerBootstrap()
    private val bossGroup = NioEventLoopGroup(1, object : ThreadFactory {
        private val threadIndex = AtomicInteger(0)
        override fun newThread(r: Runnable): Thread {
            return Thread(r, "NettyBossSelector_${this.threadIndex.incrementAndGet()}")
        }
    })

    private val workGroup = NioEventLoopGroup(8, object : ThreadFactory {
        private val threadIndex = AtomicInteger(0)
        override fun newThread(r: Runnable?): Thread {
            return Thread(r, "NettyWorkGroup_${this.threadIndex.incrementAndGet()}")
        }
    })

    init {
        server.group(bossGroup, workGroup).channel(NioServerSocketChannel::class.java)
            .option(ChannelOption.SO_BACKLOG, 1024)
            .option(ChannelOption.SO_REUSEADDR, true)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.TCP_NODELAY, true)
            .localAddress(InetSocketAddress(1111))
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    //TODO 各种控制器
                }
            })
    }

    fun start(ip: String, port: Int) {
        val future: ChannelFuture
        try {
            future = server.bind().sync()
            future.channel().closeFuture().sync()
        } catch (e: Exception) {
            bossGroup.shutdownGracefully()
            workGroup.shutdownGracefully()
        }
    }

}