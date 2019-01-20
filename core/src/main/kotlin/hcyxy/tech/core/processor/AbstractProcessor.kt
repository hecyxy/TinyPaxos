package hcyxy.tech.core.processor

import hcyxy.tech.remoting.protocol.RemotingMsg

abstract class AbstractProcessor {
    /**
     * @Description create error response
     */
    fun createErrorResponse(requestId: Long, message: String): RemotingMsg {
        return RemotingMsg.createResponse(requestId, message, null)
    }

    /**
     * @Description create successful response
     */
    fun createOkResponse(requestId: Long, message: String, body: ByteArray?): RemotingMsg {
        return RemotingMsg.createResponse(requestId, message, body)
    }
}