package hcyxy.tech.remoting.client

import hcyxy.tech.remoting.RemotingService

interface RemotingClient : RemotingService {
    fun invokeSync(addr: String)

    fun invokeAsync(addr: String)
}