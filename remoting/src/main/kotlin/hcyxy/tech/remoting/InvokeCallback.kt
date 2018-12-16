package hcyxy.tech.remoting

import hcyxy.tech.remoting.entity.Proposal

interface InvokeCallback {
    fun callback(future: ResponseFuture)
}