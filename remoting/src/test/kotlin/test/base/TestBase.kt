package test.base

import hcyxy.tech.remoting.common.BaseThread
import kotlin.concurrent.thread

class RunThread : BaseThread() {
    override fun getServiceName(): String {
        return RunThread::class.java.simpleName
    }

    override fun run() {
        var x = 1
        thread(start = true) {
            while (true) {
                x++
                this.waitForRunning(2000)
                println("${getServiceName()} $x")
                Thread.sleep(500)
            }
        }
    }
}

fun main(vararg args: String) {
    val a: BaseThread = RunThread()
    a.start()
    val b: BaseThread = RunThread()
    b.start()
    a.shutdown()
}
