package hcyxy.tech.entity

import hcyxy.tech.util.RemotingMsgSerializable
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

class RemotingMsg {
    private val logger = LoggerFactory.getLogger(RemotingMsg::class.java)
    @Transient
    private var body: ByteArray? = null

    fun encode(): ByteBuffer {
        val buffer = ByteBuffer.allocate(4)
        return buffer
    }

    fun decode(buffer: ByteBuffer): RemotingMsg {
        val length = buffer.int
        val headerLength = buffer.int
        logger.info("msg length $length headerLength $headerLength")
        val headerData = ByteArray(headerLength)
        buffer.get(headerData)
        val bodyLength = length - 4 - headerLength
        var bodyData: ByteArray? = null
        if (bodyLength > 0) {
            bodyData = ByteArray(bodyLength)
            buffer.get(bodyData)
        }
        val msg = RemotingMsgSerializable.decode(headerData, RemotingMsg::class.java)
        msg.body = bodyData
        return msg
    }
}
