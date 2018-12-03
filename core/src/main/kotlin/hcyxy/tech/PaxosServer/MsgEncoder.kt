package hcyxy.tech.PaxosServer

import hcyxy.tech.entity.RemotingMsg
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class MsgEncoder : MessageToByteEncoder<RemotingMsg>() {
    override fun encode(ctx: ChannelHandlerContext, msg: RemotingMsg, out: ByteBuf) {
        val buffer = msg.encode()
        out.writeBytes(buffer)
    }

}