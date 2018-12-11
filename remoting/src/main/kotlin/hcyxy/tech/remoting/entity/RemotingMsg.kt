package hcyxy.tech.remoting.entity

import hcyxy.tech.remoting.common.RemotingMsgSerializable
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

class RemotingMsg {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var body: ByteArray? = null

    fun encode(): ByteBuffer {
        val size = body?.size ?: 0
        val len = 4 + size
        val buffer = ByteBuffer.allocate(4 + len)
        System.out.printf("len %s  body %s ", len, size)
        buffer.putInt(len)
        buffer.putInt(size)
        buffer.put(body)
        buffer.flip()
        return buffer
    }

    fun decode(buffer: ByteBuffer): Proposal {
        println("limit :" + buffer.limit())
        val length = buffer.int
        println("total length:$length")
        val bodyLength = buffer.int
        println("header length:$bodyLength")
        val body = ByteArray(bodyLength)
        buffer.get(body)
        return RemotingMsgSerializable.decode(body, Proposal::class.java)
    }

    fun setBody(body: ByteArray) {
        this.body = body
    }
}
