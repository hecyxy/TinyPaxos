package hcyxy.tech.remoting.common

import com.dyuproject.protostuff.LinkedBuffer
import com.dyuproject.protostuff.ProtostuffIOUtil
import com.dyuproject.protostuff.Schema
import com.dyuproject.protostuff.runtime.RuntimeSchema
import org.objenesis.ObjenesisStd
import java.util.concurrent.ConcurrentHashMap

object RemotingMsgSerializable {

    private val objenesis = ObjenesisStd(true)
    private val cachedSchema = ConcurrentHashMap<Class<*>, Schema<*>>()

    private fun <T> getSchema(cls: Class<T>): Schema<T>? {
        var schema: Schema<T>? = cachedSchema[cls] as Schema<T>?
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls)
            if (schema != null) {
                cachedSchema[cls] = schema
            }
        }
        return schema
    }

    fun <T : Any> encode(obj: T): ByteArray {
        val cls = obj.javaClass
        val buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE)
        try {
            val schema = getSchema(cls)
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer)
        } catch (e: Exception) {
            throw IllegalStateException(e.message, e)
        } finally {
            buffer.clear()
        }
    }

    fun <T> decode(data: ByteArray, cls: Class<T>): T {
        try {
            val message = objenesis.newInstance(cls) as T
            val schema = getSchema(cls)
            ProtostuffIOUtil.mergeFrom(data, message, schema)
            return message
        } catch (e: Exception) {
            throw IllegalStateException(e.message, e)
        }

    }
}