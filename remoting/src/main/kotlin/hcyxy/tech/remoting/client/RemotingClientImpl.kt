package hcyxy.tech.remoting.client

import hcyxy.tech.remoting.RemotingAbstract
import hcyxy.tech.remoting.common.ChannelWrapper
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
    private val lockTime = 5L

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
        val channelWrapper = this.channelTable[addr]
        if (channelWrapper != null && channelWrapper.isOK()) {
            return channelWrapper.getChannel()
        }

        if (lock.tryLock(lockTime, TimeUnit.SECONDS)) {

        } else {

        }
        return null
    }
}

//fun main(vararg args: String) {
//    val channel = PaxosClient().connect("127.0.0.1", 11112)
//    val proposal = Proposal(EventType.ACCEPTOR, 1, null)
//    val temp = RemotingMsgSerializable.encode(proposal)
//    val remoting = RemotingMsg()
//    remoting.setBody(temp)
//    channel.writeAndFlush(remoting)
//}

