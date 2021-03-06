package hcyxy.tech.remoting

import hcyxy.tech.remoting.common.FlexibleCountDownLatch
import hcyxy.tech.remoting.common.FlexibleReleaseSemaphore
import hcyxy.tech.remoting.protocol.RemotingMsg
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ResponseFuture(
    val proposalId: Long,
    private val timeout: Long,
    private val callback: InvokeCallback?,
    private val once: FlexibleReleaseSemaphore?
) {

    @Volatile
    private var sendRequestOk = true
    @Volatile
    private var cause: Throwable? = null
    @Volatile
    private var msg: RemotingMsg? = null
    private val countDownLatch = FlexibleCountDownLatch(1)
//    private val invokeCallback: InvokeCallback? = callback
    // 保证信号量至多至少只被释放一次
//    private val once: FlexibleReleaseSemaphore? = null

    private val callbackOnce = AtomicBoolean(false)
    fun setSendRequestOk(flag: Boolean) {
        this.sendRequestOk = flag
    }

    fun setCause(cause: Throwable) {
        this.cause = cause
    }

    fun isSendRequestOk(): Boolean {
        return this.sendRequestOk
    }

    fun waitResponse(timeoutMillis: Long): RemotingMsg? {
        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS)
        return this.msg
    }

    fun putResponse(msg: RemotingMsg?) {
        this.msg = msg
    }

    fun executeCallback() {
        if (callback != null) {
            if (this.callbackOnce.compareAndSet(false, true)) {
                callback.callback(this)
            }

        }
    }

    fun release() {
        once?.release()
    }
}