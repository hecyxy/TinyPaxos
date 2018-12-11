package hcyxy.tech.remoting.server

import hcyxy.tech.remoting.RemotingAbstract
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.net.InetSocketAddress
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class RemotingServerImpl : RemotingAbstract(), RemotingServer {

    private val server = ServerBootstrap()
    //    private val  channel = PaxosClient().connect("localhost", toPort)
    private val map = HashMap<Long, Channel>()
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
            .localAddress(InetSocketAddress(2020))
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(MsgEncoder())
                        .addLast(MsgDecoder())
                        .addLast(ConnectManager())
                        .addLast(ServerHandler(map))
                }
            })
    }

    override fun start() {
        val future: ChannelFuture
        try {
            future = server.bind().sync()
            future.channel().closeFuture().sync()
        } catch (e: Exception) {
            bossGroup.shutdownGracefully()
            workGroup.shutdownGracefully()
            throw e
        }
    }

    override fun invokeSync(channel: Channel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun invokeAsync(channel: Channel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun shutdown() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}