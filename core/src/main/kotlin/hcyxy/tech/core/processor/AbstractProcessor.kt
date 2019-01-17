package hcyxy.tech.core.processor

import hcyxy.tech.remoting.protocol.RemotingMsg

abstract class AbstractProcessor {
    /**
     * @Description create error response
     */
    fun createErrorResponse(message: String): RemotingMsg {
        return RemotingMsg.createResponse(message, null)
    }

    /**
     * @Description create successful response
     */
    fun createOkResponse(message: String): RemotingMsg {
        return RemotingMsg.createResponse(message, null)
    }
}