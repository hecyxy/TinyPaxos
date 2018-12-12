package hcyxy.tech.remoting.common

import java.util.concurrent.TimeUnit

class FlexibleCountDownLatch(count: Int) {
    private val sync: Sync

    val count: Long
        get() = sync.count.toLong()

    init {
        if (count < 0)
            throw IllegalArgumentException("count < 0")
        this.sync = Sync(count)
    }

    fun await() {
        sync.acquireSharedInterruptibly(1)
    }

    fun await(timeout: Long, unit: TimeUnit): Boolean {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout))
    }

    fun countDown() {
        sync.releaseShared(1)
    }

    fun reset() {
        sync.reset()
    }

    override fun toString(): String {
        return "${super.toString()} Count: ${sync.count}"
    }
}