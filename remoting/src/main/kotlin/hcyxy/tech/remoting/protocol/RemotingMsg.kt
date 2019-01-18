package hcyxy.tech.remoting.protocol

import hcyxy.tech.remoting.common.RemotingMsgSerializable
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong

class RemotingMsg {
    companion object {
        private val increment = AtomicLong(0)
        fun decode(buffer: ByteBuffer): RemotingMsg {
            val length = buffer.int//buffer.limit()
            val headerLength = buffer.int
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

        fun createResponse(
            requestId: Long,
            message: String?,
            body: ByteArray?
        ): RemotingMsg {
            val msg = RemotingMsg()
            msg.setRequestId(requestId)
            msg.setActionCode(ActionCode.RESPONSE.code)
            message?.let { msg.setMessage(it) }
            msg.setRemotingCode(RemotingCode.NORMAL.code)
            body?.let { msg.setBody(body) }
            return msg
        }

        fun createRequest(
            processorCode: Int,
            message: String?,
            processorEventType: Int,
            body: ByteArray?
        ): RemotingMsg {
            val msg = RemotingMsg()
            msg.setActionCode(ActionCode.REQUEST.code)
            msg.setProcessorCode(processorCode)
            message?.let { msg.setMessage(it) }
            msg.setRemotingCode(RemotingCode.NORMAL.code)
            msg.setProcessorEventType(processorEventType)
            body?.let { msg.setBody(body) }
            return msg
        }
    }

    /**
     * @Description header message code packet
     */
    private var requestId = increment.incrementAndGet()
    //request code request or response
    private var actionCode: Int = ActionCode.REQUEST.code
    //processor
    private var processorCode: Int = 0
    //remoting description
    private var message: String = ""
    private var remotingCode: Int = 0
    //processor event type
    private var processorEventType: Int = 0
    @Transient
    private var header: ByteArray? = null
    /**
     * @Description body include message code packet
     */
    @Transient
    private var body: ByteArray? = null


    fun encode(): ByteBuffer {
        this.builderHeader()
        var len = 4
        val headerSize = header?.size ?: 0
        val bodySize = body?.size ?: 0
        len += headerSize + bodySize
        val buffer = ByteBuffer.allocate(4 + len)
        buffer.putInt(len)
        buffer.putInt(headerSize)
        buffer.put(header)
        body?.let { buffer.put(it) }
        buffer.flip()
        return buffer
    }


    fun setBody(body: ByteArray) {
        this.body = body
    }

    private fun builderHeader() {
        this.header = RemotingMsgSerializable.encode(this)
    }

    fun getActionCode(): Int {
        return this.actionCode
    }

    fun getProcessorCode(): Int {
        return this.processorCode
    }

    fun getRequestId(): Long {
        return this.requestId
    }

    fun setRequestId(requestId: Long) {
        this.requestId = requestId
    }

    fun getBody(): ByteArray? {
        return body
    }

    fun getHeader(): ByteArray? {
        return header
    }

    fun setActionCode(actionCode: Int) {
        this.actionCode = actionCode
    }

    fun setProcessorCode(processorCode: Int) {
        this.processorCode = processorCode
    }

    fun setMessage(message: String) {
        this.message = message
    }

    fun getMessage(): String {
        return this.message
    }

    fun getProcessorEventType(): Int {
        return this.processorEventType
    }

    fun setProcessorEventType(eventType: Int) {
        this.processorEventType = eventType
    }

    fun setRemotingCode(remotingCode: Int) {
        this.remotingCode = remotingCode
    }

    fun getRemotingCode(): Int {
        return this.remotingCode
    }

    override fun toString(): String {
        return "requestId: $requestId, actionCode: $actionCode, processorCode: $processorCode, message: $message"
    }
}
