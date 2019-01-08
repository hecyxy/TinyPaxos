package hcyxy.tech.remoting.util

import hcyxy.tech.remoting.entity.Proposal

fun Proposal.setRequestId(requestId: Long): Proposal {
    this.requestId = requestId
    return this
}