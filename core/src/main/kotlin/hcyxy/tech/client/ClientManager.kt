package hcyxy.tech.client

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import org.slf4j.LoggerFactory
import java.net.SocketAddress

class ClientManager : ChannelDuplexHandler() {
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
        logger.info("NETTY CLIENT PIPELINE: DISCONNECT {}", remoteAddress)
        super.disconnect(ctx, promise)
    }


    override fun close(ctx: ChannelHandlerContext, promise: ChannelPromise) {
        val remoteAddress = ctx.channel().remoteAddress().toString()
        logger.info("NETTY CLIENT PIPELINE: CLOSE {}", remoteAddress)
        super.close(ctx, promise)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        val remoteAddress = ctx.channel().remoteAddress().toString()
        logger.warn("NETTY CLIENT PIPELINE: exceptionCaught {}", remoteAddress)
        logger.warn("NETTY CLIENT PIPELINE: exceptionCaught exception.", cause)
    }
}