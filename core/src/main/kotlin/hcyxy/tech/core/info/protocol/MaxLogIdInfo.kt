package hcyxy.tech.core.info.protocol

import hcyxy.tech.remoting.common.RemotingMsgSerializable


class MaxLogIdInfo {
    private var logId: Long = 0

    fun setLogId(logId: Long) {
        this.logId = logId
    }

    fun getLogId(): Long {
        return this.logId
    }

    fun getByteArray(): ByteArray {
        return RemotingMsgSerializable.encode(this)
    }

    companion object {
        fun getObject(body: ByteArray): MaxLogIdInfo {
            return RemotingMsgSerializable.decode(body, MaxLogIdInfo::class.java)
        }
    }

}