package hcyxy.tech.remoting.common

import java.util.concurrent.locks.AbstractQueuedSynchronizer

class Sync(private val initCount: Int) : AbstractQueuedSynchronizer() {
    init {
        state = initCount
    }

    val count: Int
        get() = state

    fun reset() {
        state = initCount
    }

    override fun tryReleaseShared(arg: Int): Boolean {
        while (true) {
            val current = state
            if (current == 0)
                return false
            val next = current - 1
            if (compareAndSetState(current, next)) {
                return next == 0
            }
        }
    }

    override fun tryAcquireShared(arg: Int): Int {
        return if (state == 0) 1 else -1
    }
}