package hcyxy.tech.remoting.server

import hcyxy.tech.remoting.entity.RemotingMsg
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import org.slf4j.LoggerFactory

class MsgDecoder : LengthFieldBasedFrameDecoder(FRAME_MAX_LENGTH, 0, 4, 0, 0) {
    companion object {
        private val logger = LoggerFactory.getLogger(MsgDecoder::class.java)
        private val FRAME_MAX_LENGTH = 888888
    }

    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf): Any? {
        var frame: ByteBuf? = null
        try {
            frame = super.decode(ctx, `in`) as ByteBuf
            val byteBuffer = frame.nioBuffer()
            return RemotingMsg().decode(byteBuffer)
        } catch (e: Exception) {
            logger.info("decode error")
            ctx.channel().close().addListener { future ->
                logger.info("close channel {}", future.isSuccess)
            }
        } finally {
            frame?.release()
        }
        return null
    }
}
