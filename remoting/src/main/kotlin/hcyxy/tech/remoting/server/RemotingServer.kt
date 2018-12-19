package hcyxy.tech.remoting.server

import hcyxy.tech.remoting.InvokeCallback
import hcyxy.tech.remoting.RemotingService
import hcyxy.tech.remoting.entity.Proposal
import io.netty.channel.Channel

interface RemotingServer : RemotingService {
    fun invokeSync(channel: Channel, proposal: Proposal, timeout: Long): Proposal

    fun invokeAsync(channel: Channel, proposal: Proposal, timeout: Long, callBack: InvokeCallback)
}