package hcyxy.tech.core.util

import hcyxy.tech.remoting.common.RemotingMsgSerializable


fun <T : Any> T.getByteArray(): ByteArray {
    return RemotingMsgSerializable.encode(this)
}

fun <T> getObject(clz: Class<T>, body: ByteArray): T {
    return RemotingMsgSerializable.decode(body, clz)
}