package hcyxy.tech.core.service

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

class ThreadFactoryImpl(prefixStr: String) : ThreadFactory {
    private val threadIndex = AtomicLong(0)
    private var prefix: String = prefixStr

    override fun newThread(r: Runnable): Thread {
        return Thread(r, prefix + threadIndex.incrementAndGet())
    }
}