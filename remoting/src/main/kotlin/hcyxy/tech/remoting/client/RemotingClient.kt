package hcyxy.tech.remoting.client

import hcyxy.tech.remoting.InvokeCallback
import hcyxy.tech.remoting.RemotingService
import hcyxy.tech.remoting.protocol.RemotingMsg

interface RemotingClient : RemotingService {
    fun invokeSync(addr: String, msg: RemotingMsg, timeout: Long): RemotingMsg

    fun invokeAsync(addr: String, msg: RemotingMsg, timeout: Long, callBack: InvokeCallback)

    fun invokeOnce(addr: String, msg: RemotingMsg, timeout: Long)
}