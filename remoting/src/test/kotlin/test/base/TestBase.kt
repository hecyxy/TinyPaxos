package test.base

import hcyxy.tech.remoting.common.BaseThread
import hcyxy.tech.remoting.common.RemotingHelper
import java.net.InetSocketAddress
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
    val addr = "100.73.80.21:8090"
    val socket = InetSocketAddress("100.73.41.30",8090)
    println(socket.toString())
}
