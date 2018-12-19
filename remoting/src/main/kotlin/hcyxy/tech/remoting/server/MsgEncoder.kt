package hcyxy.tech.remoting.server

import hcyxy.tech.remoting.common.RemotingMsgSerializable
import hcyxy.tech.remoting.entity.Proposal
import hcyxy.tech.remoting.entity.RemotingMsg
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class MsgEncoder : MessageToByteEncoder<Proposal>() {
    override fun encode(ctx: ChannelHandlerContext, msg: Proposal, out: ByteBuf) {
        val a = RemotingMsg()
        a.setBody(RemotingMsgSerializable.encode(msg))
        val buffer = a.encode()
        out.writeBytes(buffer)
    }

}