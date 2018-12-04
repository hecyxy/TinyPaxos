package hcyxy.tech.server

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import org.slf4j.LoggerFactory

class ConnectManager : ChannelDuplexHandler() {
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
        super.channelUnregistered(ctx)
    }

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        val remoteAddress = ctx.channel().remoteAddress().toString()
        logger.info("server pipeline:channelActive,the channel[{}]", remoteAddress)
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

}