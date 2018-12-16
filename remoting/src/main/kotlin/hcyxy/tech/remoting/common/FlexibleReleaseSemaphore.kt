package hcyxy.tech.remoting.common

import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger

/**
 * 信号量几次释放
 */
class FlexibleReleaseSemaphore(private val semaphore: Semaphore?, private val count: Int) {
    private val atomicInteger = AtomicInteger(0)
    
    fun release() {
        if (semaphore != null) {
            if (atomicInteger.get() < count) {
                atomicInteger.incrementAndGet()
            }
        }
    }
}