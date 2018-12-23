package test.paxos

import hcyxy.tech.remoting.InvokeCallback
import hcyxy.tech.remoting.ResponseFuture

class ClientCallback : InvokeCallback {
    override fun callback(future: ResponseFuture) {
        println("callback: ${future.proposalId}")
    }
}