package hcyxy.tech.core.util

import hcyxy.tech.remoting.common.RemotingMsgSerializable


fun <T : Any> T.getByteArray(): ByteArray {
    return RemotingMsgSerializable.encode(this)
}