package hcyxy.tech.remoting.common

import org.slf4j.LoggerFactory

abstract class ServiceThread : Runnable {
    private val logger = LoggerFactory.getLogger(javaClass)
    // 执行线程
    private val thread: Thread
    // 线程回收时间，默认90S
    private val joinTime = 60 * 1000L
    // 是否已经被Notify过
    @Volatile
    protected var hasNotified = false
    // 线程是否已经停止
    @Volatile
    protected var stoped = false

    private val lock = Object()

    init {
        this.thread = Thread(this, this.getServiceName())
    }

    abstract fun getServiceName(): String


    fun start() {
        this.thread.start()
    }


    fun shutdown() {
        this.shutdown(true)
    }

    private fun shutdown(interrupt: Boolean) {
        this.stoped = true
        logger.info("shutdown thread ${this.getServiceName()} interrupt $interrupt")
        synchronized(lock) {
            if (!this.hasNotified) {
                this.hasNotified = true
                lock.notify()
            }
        }

        try {
            if (interrupt) {
                this.thread.interrupt()
            }

            val beginTime = System.currentTimeMillis()
            if (!this.thread.isDaemon) {
                this.thread.join(this.getJointime())
            }
            val eclipseTime = System.currentTimeMillis() - beginTime
            logger.info(
                "join thread " + this.getServiceName() + " eclipse time(ms) " + eclipseTime + " "
                        + this.getJointime()
            )
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }


    fun wakeup() {
        synchronized(lock) {
            if (!this.hasNotified) {
                this.hasNotified = true
                lock.notify()
            }
        }
    }

    protected fun waitForRunning(interval: Long) {
        synchronized(lock) {
            if (this.hasNotified) {
                this.hasNotified = false
                this.onWaitEnd()
                return
            }

            try {
                lock.wait(interval)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                this.hasNotified = false
                this.onWaitEnd()
            }
        }
    }


    protected fun onWaitEnd() {}

    fun isStoped(): Boolean {
        return stoped
    }


    fun getJointime(): Long {
        return joinTime
    }

}

