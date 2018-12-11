package hcyxy.tech.remoting.common

import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


/**
 * @Description 线程抽象类
 */
abstract class BaseThread : Runnable {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val joinTime = 60 * 1000L

    private val thread: Thread
    private val wait = FlexibleCountDownLatch(1)
    @Volatile
    protected var hasNotified = AtomicBoolean(false)
    @Volatile
    protected var stopped = false

    init {
        this.thread = Thread(this, this.getServiceName())
    }

    abstract fun getServiceName(): String

    fun start() {
        this.thread.start()
    }

    fun shutdown() {
        this.shutdown(false)
    }

    fun shutdown(interrupt: Boolean) {
        this.stopped = true
        logger.info("shutdown thread ${this.getServiceName()}  interrupt $interrupt")

        if (hasNotified.compareAndSet(false, true)) {
            wait.countDown()
        }
        try {
            if (interrupt) {
                this.thread.interrupt()
            }
            val beginTime = System.currentTimeMillis()
            if (!this.thread.isDaemon) {
                this.thread.join(this.getJointime())
            }
            val cost = System.currentTimeMillis() - beginTime
            logger.info("join thread ${this.getServiceName()}  cost $cost ${this.getJointime()}")
        } catch (e: InterruptedException) {
            logger.error("Interrupted", e)
        }

    }

    fun getJointime(): Long {
        return joinTime
    }

    fun stop() {
        this.stop(false)
    }

    fun stop(interrupt: Boolean) {
        this.stopped = true
        logger.info("stop thread ${this.getServiceName()} interrupt  $interrupt")

        if (hasNotified.compareAndSet(false, true)) {
            wait.countDown()
        }

        if (interrupt) {
            this.thread.interrupt()
        }
    }

    fun makeStop() {
        this.stopped = true
        logger.info("makestop thread ${this.getServiceName()}")
    }

    fun wakeup() {
        if (hasNotified.compareAndSet(false, true)) {
            wait.countDown() // notify
        }
    }

    protected fun waitForRunning(interval: Long) {
        if (hasNotified.compareAndSet(true, false)) {
            this.onWaitEnd()
            return
        }
        wait.reset()
        try {
            wait.await(interval, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            logger.error("Interrupted", e)
        } finally {
            hasNotified.set(false)
            this.onWaitEnd()
        }
    }

    private fun onWaitEnd() {}

    fun isStopped(): Boolean {
        return stopped
    }

}

