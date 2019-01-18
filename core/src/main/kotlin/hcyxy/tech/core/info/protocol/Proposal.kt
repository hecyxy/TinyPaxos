package hcyxy.tech.core.info.protocol

import hcyxy.tech.remoting.common.RemotingMsgSerializable

class Proposal {
    private var proposalId: Long = 0

    fun setProposalId(proposalId: Long) {
        this.proposalId = proposalId
    }

    fun getProposalId(): Long {
        return this.proposalId
    }

    fun getByteArray(): ByteArray {
        return RemotingMsgSerializable.encode(this)
    }

    companion object {
        fun getObject(body: ByteArray): Proposal {
            return RemotingMsgSerializable.decode(body, Proposal::class.java)
        }
    }
}