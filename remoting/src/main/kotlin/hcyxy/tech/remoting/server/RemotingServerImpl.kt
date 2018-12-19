package hcyxy.tech.remoting.server

import hcyxy.tech.remoting.InvokeCallback
import hcyxy.tech.remoting.RemotingAbstract
import hcyxy.tech.remoting.common.RemotingHelper
import hcyxy.tech.remoting.entity.EventType
import hcyxy.tech.remoting.entity.Proposal
import hcyxy.tech.remoting.entity.RemotingMsg
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleStateHandler
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.log

class RemotingServerImpl : RemotingAbstract(), RemotingServer {
    private val logger = LoggerFactory.getLogger(RemotingServerImpl::class.java)
    private val server: ServerBootstrap = ServerBootstrap()
    private var workGroup: NioEventLoopGroup
    private var bossGroup: NioEventLoopGroup
    private var port = 0
    private val activeConn = AtomicInteger(0)

    init {
        workGroup = NioEventLoopGroup(8, object : ThreadFactory {
            private val threadIndex = AtomicInteger(0)
            override fun newThread(r: Runnable?): Thread {
                return Thread(r, "NettyWorkGroup_${this.threadIndex.incrementAndGet()}")
            }
        })
        bossGroup = NioEventLoopGroup(1, object : ThreadFactory {
            private val threadIndex = AtomicInteger(0)
            override fun newThread(r: Runnable?): Thread {
                return Thread(r, "NettyBossSelector_${this.threadIndex.incrementAndGet()}")
            }
        })
    }

    override fun start() {
        this.server.group(bossGroup, workGroup).channel(NioServerSocketChannel::class.java)
            .option(ChannelOption.SO_BACKLOG, 1024)
            .option(ChannelOption.SO_REUSEADDR, true)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.TCP_NODELAY, true)
            .localAddress(InetSocketAddress(8888))
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(MsgEncoder())
                        .addLast(MsgDecoder())
                        .addLast(IdleStateHandler(0, 0, 120))
                        .addLast(ConnectManager())
                        .addLast(ServerHandler())
                }
            })
        try {
            val future = server.bind().sync()
            val addr = future.channel().localAddress() as InetSocketAddress
            this.port = addr.port
        } catch (e: Exception) {
            bossGroup.shutdownGracefully()
            workGroup.shutdownGracefully()
            logger.error("server bin ${this.port} exception", e)
        }
    }

    override fun invokeSync(channel: Channel, proposal: Proposal, timeout: Long): Proposal {
        return invokeSyncImpl(channel, proposal, timeout)
    }

    override fun invokeAsync(channel: Channel, proposal: Proposal, timeout: Long, callback: InvokeCallback) {
        invokeAsyncImpl(channel, proposal, timeout, callback)
    }

    override fun shutdown() {
        try {
            val bossFuture = this.workGroup.shutdownGracefully()
            val workerFuture = this.bossGroup.shutdownGracefully()
            bossFuture.await()
            workerFuture.await()
        } catch (e: Exception) {
            logger.error("shut down error", e)
        }
    }


    internal inner class ConnectManager : ChannelDuplexHandler() {
        private val logger = LoggerFactory.getLogger(ConnectManager::class.java)

        @Throws(Exception::class)
        override fun channelRegistered(ctx: ChannelHandlerContext) {
            val remoteAddress = ctx.channel().remoteAddress().toString()
            logger.debug("receive msg from ... {}", remoteAddress)
            super.channelRegistered(ctx)
        }

        @Throws(Exception::class)
        override fun channelUnregistered(ctx: ChannelHandlerContext) {
            val remoteAddress = ctx.channel().remoteAddress().toString()
            logger.debug("unregister ip ... {}", remoteAddress)
            activeConn.decrementAndGet()
            super.channelUnregistered(ctx)
        }

        @Throws(Exception::class)
        override fun channelActive(ctx: ChannelHandlerContext) {
            val remoteAddress = ctx.channel().remoteAddress().toString()
            logger.info("server pipeline:channelActive,the channel[{}]", remoteAddress)
            activeConn.incrementAndGet()
            super.channelActive(ctx)
        }

        @Throws(Exception::class)
        override fun channelInactive(ctx: ChannelHandlerContext) {
            val remoteAddress = ctx.channel().remoteAddress().toString()
            logger.info("server pipeline:channelInActive,the channel[{}]", remoteAddress)
            super.channelInactive(ctx)
        }

        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            val remoteAddress = ctx.channel().remoteAddress().toString()
            logger.warn("netty server pipeline:exception caught{}", remoteAddress)
            logger.warn("caught: {}", cause)
            val channel = ctx.channel()
            channel.close().addListener { future ->
                logger.info(
                    "close connection address {},result {}",
                    remoteAddress,
                    future.isSuccess
                )
            }
        }

        override fun userEventTriggered(ctx: ChannelHandlerContext, event: Any) {
            if (event is IdleStateEvent) {
                if (event.state() == IdleState.ALL_IDLE) {
                    val remoteAddress = RemotingHelper.channel2Addr(ctx.channel())
                    logger.warn("netty server idle exception [{}]", remoteAddress)
                    RemotingHelper.closeChannel(ctx.channel())
                }
            }

            ctx.fireUserEventTriggered(event)
        }
    }

    internal inner class ServerHandler : SimpleChannelInboundHandler<Proposal>() {
        private val logger = LoggerFactory.getLogger(ServerHandler::class.java)

        override fun channelRead0(ctx: ChannelHandlerContext, msg: Proposal) {
            logger.info("receive: $msg")
            processReceiveMessage(ctx, msg)
        }

    }

}