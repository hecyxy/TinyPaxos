package hcyxy.tech.remoting.client

import hcyxy.tech.remoting.InvokeCallback
import hcyxy.tech.remoting.RemotingAbstract
import hcyxy.tech.remoting.common.ChannelWrapper
import hcyxy.tech.remoting.common.RemotingHelper
import hcyxy.tech.remoting.entity.Proposal
import hcyxy.tech.remoting.exception.RemotingConnectException
import hcyxy.tech.remoting.server.MsgDecoder
import hcyxy.tech.remoting.server.MsgEncoder
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleStateHandler
import org.slf4j.LoggerFactory
import java.net.SocketAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

class RemotingClientImpl : RemotingAbstract(1000), RemotingClient {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val boot = Bootstrap()
    private var workerGroup: EventLoopGroup? = null
    //保存长连接
    private val channelTable = ConcurrentHashMap<String, ChannelWrapper>()
    private val lock = ReentrantLock()
    private val lockTime = 5000L
    private val channelWait = 2000L

    init {
        this.workerGroup = NioEventLoopGroup(1, object : ThreadFactory {
            val index = AtomicInteger(0)
            override fun newThread(r: Runnable): Thread {
                return Thread(r, "client-Thread${index.incrementAndGet()}")
            }
        })
    }


    override fun start() {
        this.boot.group(workerGroup).channel(NioSocketChannel::class.java)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_KEEPALIVE, false)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline()
                        .addLast(MsgEncoder())
                        .addLast(MsgDecoder())
                        .addLast(IdleStateHandler(0, 0, 120))
                        .addLast(ClientManager())
                        .addLast(ClientHandler())
                }
            })

    }

    override fun shutdown() {
        this.channelTable.clear()
    }

    override fun invokeSync(addr: String, proposal: Proposal, timeout: Long): Proposal {
        val channel = getOrCreateChanne(addr)
        if (channel != null && channel.isActive) {
            try {
                return invokeSyncImpl(channel, proposal, timeout)
            } catch (e: Exception) {
                logger.error("invoke sync,exception", e)
                throw RemotingConnectException(addr, e)
            }
        } else {
            this.closeChannel(addr, channel)
            throw RemotingConnectException(addr)
        }
    }

    override fun invokeAsync(addr: String, proposal: Proposal, timeout: Long, callBack: InvokeCallback) {
        val channel = getOrCreateChanne(addr)
        if (channel != null && channel.isActive) {
            try {
                this.invokeAsyncImpl(channel, proposal, timeout, callBack)
            } catch (e: Exception) {
                logger.error("invoke async exception", e)
            }
        } else {
            this.closeChannel(addr, channel)
            throw RemotingConnectException(addr)
        }
    }


    private fun getOrCreateChanne(addr: String): Channel? {
        val channelWrapper = this.channelTable[addr]
        if (channelWrapper != null && channelWrapper.isOK()) {
            return channelWrapper.getChannel()
        }

        return createChannel(addr)
    }

    private fun createChannel(addr: String): Channel? {
        var channelWrapper = this.channelTable[addr]
        if (channelWrapper != null && channelWrapper.isOK()) {
            return channelWrapper.getChannel()
        }
        if (lock.tryLock(lockTime, TimeUnit.SECONDS)) {
            try {
                var createdConn = false
                channelWrapper = this.channelTable[addr]
                createdConn = if (channelWrapper != null) {
                    if (channelWrapper.isOK()) {
                        return channelWrapper.getChannel()
                    } else if (!channelWrapper.getChannelFuture().isDone) {
                        false //正在连接中
                    } else {
                        this.channelTable.remove(addr)
                        true
                    }
                } else {
                    true
                }
                if (createdConn) {
                    val channelFuture = this.boot.connect(RemotingHelper.string2Addr(addr))
                    channelWrapper = ChannelWrapper(channelFuture)
                    this.channelTable[addr] = channelWrapper
                }
            } catch (e: Exception) {
                logger.error("create channel error ", e)
            } finally {
                lock.unlock()
            }
        } else {
            logger.warn("lock timeout,create channel failed")
        }
        if (channelWrapper != null) {
            val channelFuture = channelWrapper.getChannelFuture()
            if (channelFuture.awaitUninterruptibly(channelWait, TimeUnit.MILLISECONDS)) {
                if (channelWrapper.isOK()) {
                    logger.info("createChannel: connect remote host[{}] success, {}", addr, channelFuture.toString())
                    return channelWrapper.getChannel()
                } else {
                    logger.warn(
                        "createChannel: connect remote host[{}] failed,{} cause:",
                        addr,
                        channelFuture.toString(),
                        channelFuture.cause()
                    )
                }
            } else {
                logger.warn(
                    "createChannel: connect remote host[{}] timeout {}ms, {}",
                    addr,
                    channelWait,
                    channelFuture.toString()
                )
            }
        }
        return null
    }

    private fun closeChannel(addr: String, channel: Channel?) {
        if (channel == null) {
            return
        }
        try {
            if (lock.tryLock(lockTime, TimeUnit.MILLISECONDS)) {
                var removedChannel = true
                val prev = this.channelTable[addr]
                if (null == prev) {
                    logger.info("the channel id:{} has been removed from the table", addr)
                } else if (prev.getChannel() != channel) {
                    removedChannel = false
                    logger.info("the channel has been moved before,a new channel has been created!")
                }

                if (removedChannel) {
                    this.channelTable.remove(addr)
                    logger.info("has moved the channel from the table")
                }

            } else {
                logger.warn("try to lock table,but timeout")
            }
        } catch (e: Exception) {
            logger.error("close channel exception ", e)
        } finally {
            lock.unlock()
        }
    }

    fun closeChannel(channel: Channel) {
        try {
            if (lock.tryLock(lockTime, TimeUnit.MILLISECONDS)) {
                this.channelTable.forEach { k, v ->
                    if (v.getChannel() == channel) {
                        this.channelTable.remove(k)
                        logger.info("remove the channel,addr:{}", k)
                    }
                }
            } else {
                logger.warn("try to lock the table,but timeout")
            }
        } catch (e: Exception) {
            logger.error("close the channel,exception occur", e)
        } finally {
            lock.unlock()
        }
    }

    internal inner class ClientManager : ChannelDuplexHandler() {
        private val logger = LoggerFactory.getLogger(javaClass)
        override fun connect(
            ctx: ChannelHandlerContext,
            remoteAddress: SocketAddress?,
            localAddress: SocketAddress?,
            promise: ChannelPromise
        ) {
            val local = localAddress?.toString() ?: "unknown"
            val remote = remoteAddress?.toString() ?: "unknown"
            logger.info("client pipeline :connect {} => {}", local, remote)
            super.connect(ctx, remoteAddress, localAddress, promise)
        }

        override fun disconnect(ctx: ChannelHandlerContext, promise: ChannelPromise) {
            val remoteAddress = ctx.channel().remoteAddress().toString()
            logger.info("netty client pipeline disconnect {}", remoteAddress)
            closeChannel(ctx.channel())
            super.disconnect(ctx, promise)
        }


        override fun close(ctx: ChannelHandlerContext, promise: ChannelPromise) {
            val remoteAddress = ctx.channel().remoteAddress().toString()
            logger.info("client pipeline close {}", remoteAddress)
            closeChannel(ctx.channel())
            super.close(ctx, promise)
        }

        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            val remoteAddress = ctx.channel().remoteAddress().toString()
            logger.warn("remoteAddress:{},netty client pipeline exceptionCaught exception.", remoteAddress, cause)
            closeChannel(ctx.channel())
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

    internal inner class ClientHandler : SimpleChannelInboundHandler<Proposal>() {
        private val logger = LoggerFactory.getLogger(javaClass)
        override fun channelRead0(ctx: ChannelHandlerContext, msg: Proposal) {
            logger.info("read message")
            processReceiveMessage(ctx, msg)
        }
    }

}

