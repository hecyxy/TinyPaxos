package hcyxy.tech.remoting.client

import hcyxy.tech.remoting.InvokeCallback
import hcyxy.tech.remoting.RemotingService
import hcyxy.tech.remoting.entity.Proposal

interface RemotingClient : RemotingService {
    fun invokeSync(addr: String, proposal: Proposal, timeout: Long): Proposal

    fun invokeAsync(addr: String, proposal: Proposal, timeout: Long, callBack: InvokeCallback)
}