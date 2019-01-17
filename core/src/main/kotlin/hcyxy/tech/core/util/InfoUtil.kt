package hcyxy.tech.core.util

import hcyxy.tech.core.info.SubmitValue
import hcyxy.tech.core.info.protocol.MaxLogIdInfo
import hcyxy.tech.remoting.common.RemotingMsgSerializable
import org.slf4j.LoggerFactory

object InfoUtil {
    private val logger = LoggerFactory.getLogger(javaClass)
    fun byte2MaxLogIdInfo(byte: ByteArray): MaxLogIdInfo? {
        return try {
            RemotingMsgSerializable.decode(byte, MaxLogIdInfo::class.java)
        } catch (e: Exception) {
            logger.error("decode error", e)
            null
        }
    }

    fun byte2SubmitValue(byte: ByteArray): SubmitValue? {
        return try {
            RemotingMsgSerializable.decode(byte, SubmitValue::class.java)
        } catch (e: Exception) {
            logger.error("decode error", e)
            null
        }
    }
}