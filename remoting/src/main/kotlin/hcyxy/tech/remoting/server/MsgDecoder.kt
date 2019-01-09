package hcyxy.tech.remoting.server

import hcyxy.tech.remoting.protocol.RemotingMsg
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import org.slf4j.LoggerFactory

class MsgDecoder : LengthFieldBasedFrameDecoder(FRAME_MAX_LENGTH, 0, 4, 0, 0) {
    companion object {
        private val logger = LoggerFactory.getLogger(MsgDecoder::class.java)
        private const val FRAME_MAX_LENGTH = 888888
    }

    override fun decode(ctx: ChannelHandlerContext, byteBuf: ByteBuf): Any? {
        var meta: ByteBuf? = null
        try {
            meta = super.decode(ctx, byteBuf) as? ByteBuf
            if (meta == null) {
                return null
            }
            val byteBuffer = meta.nioBuffer()
            return RemotingMsg.decode(byteBuffer)
        } catch (e: Exception) {
            logger.error("decode error", e)
            ctx.channel().close().addListener { future ->
                logger.info("close channel {}", future.isSuccess)
            }
        } finally {
            meta?.release()
        }
        return null
    }
}
