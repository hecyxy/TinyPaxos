package hcyxy.tech.client

import hcyxy.tech.entity.EventType
import hcyxy.tech.entity.Packet
import hcyxy.tech.entity.Proposal
import hcyxy.tech.entity.RemotingMsg
import hcyxy.tech.server.MsgDecoder
import hcyxy.tech.server.MsgEncoder
import hcyxy.tech.util.RemotingMsgSerializable
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
                return Thread(r, "client-Thread${index.incrementAndGet()}")
            }
        })

        boot = Bootstrap()
        boot.group(workerGroup).channel(NioSocketChannel::class.java)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_KEEPALIVE, false)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(MsgEncoder())
                        .addLast(MsgDecoder())
                        .addLast(ClientManager())
                        .addLast(ClientHandler())
                }
            })
    }

    fun connect(ip: String, port: Int): Channel {
        try {
            future = boot.connect(ip, port)
            return future.channel()
        } catch (e: Exception) {
            logger.error("error $e")
            throw  e
        }
    }
}

fun main(vararg args: String) {
    val channel = PaxosClient().connect("127.0.0.1", 11112)
    val proposal = Proposal(EventType.ACCEPTOR, 1, null)
    val temp = RemotingMsgSerializable.encode(proposal)
    val remoting = RemotingMsg()
    remoting.setBody(temp)
    channel.writeAndFlush(remoting)
}