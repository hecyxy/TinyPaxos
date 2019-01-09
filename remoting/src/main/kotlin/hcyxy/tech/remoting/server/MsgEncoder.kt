package hcyxy.tech.remoting.server

import hcyxy.tech.remoting.common.RemotingHelper
import hcyxy.tech.remoting.protocol.RemotingMsg
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.slf4j.LoggerFactory
import java.lang.Exception

class MsgEncoder : MessageToByteEncoder<RemotingMsg>() {
    private val logger = LoggerFactory.getLogger(javaClass)
    override fun encode(ctx: ChannelHandlerContext, msg: RemotingMsg, out: ByteBuf) {
        try {
            val data = msg.encode()
            out.writeBytes(data)
        } catch (e: Exception) {
            logger.error("encode error", e)
            logger.error("remoting msg: $msg")
            RemotingHelper.closeChannel(ctx.channel())
        }
    }
}