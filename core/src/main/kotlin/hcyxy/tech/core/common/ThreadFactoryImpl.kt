package hcyxy.tech.core.common

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

class ThreadFactoryImpl : ThreadFactory {
    private val threadIndex = AtomicLong(0)
    private var prefix: String

    constructor(prefixStr: String) {
        this.prefix = prefixStr
    }

    override fun newThread(r: Runnable): Thread {
        return Thread(r, prefix + threadIndex.incrementAndGet())
    }
}