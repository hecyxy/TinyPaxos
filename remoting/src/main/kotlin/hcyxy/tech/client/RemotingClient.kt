package hcyxy.tech.client

import hcyxy.tech.RemotingService

interface RemotingClient : RemotingService {
    fun invokeSync(addr: String)

    fun invokeAsync(addr: String)
}