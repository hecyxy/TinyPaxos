package hcyxy.tech.remoting.client

import hcyxy.tech.remoting.RemotingAbstract
import hcyxy.tech.remoting.common.ChannelWrapper
import hcyxy.tech.remoting.common.RemotingHelper
import hcyxy.tech.remoting.server.MsgDecoder
import hcyxy.tech.remoting.server.MsgEncoder
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.timeout.IdleStateHandler
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

class RemotingClientImpl : RemotingAbstract(), RemotingClient {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val boot = Bootstrap()
    private var workerGroup: EventLoopGroup? = null
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

    override fun invokeSync(addr: String) {
        getOrCreateChanne(addr)
        //TODO
    }

    override fun invokeAsync(addr: String) {
        //TODO
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
                if (channelWrapper != null) {
                    if (channelWrapper.isOK()) {
                        return channelWrapper.getChannel()
                    } else if (!channelWrapper.getChannelFuture().isDone) {
                        createdConn = false //正在连接中
                    } else {
                        this.channelTable.remove(addr)
                        createdConn = true
                    }
                } else {
                    createdConn = true
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

    fun closeChannel(addr: String, channel: Channel) {
        try {
            if (lock.tryLock(lockTime, TimeUnit.MILLISECONDS)) {
                var removedChannel = true

            }
        } catch (e: Exception) {

        } finally {
            lock.unlock()
        }
    }
}

