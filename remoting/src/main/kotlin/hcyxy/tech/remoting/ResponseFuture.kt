package hcyxy.tech.remoting

import hcyxy.tech.remoting.common.FlexibleCountDownLatch
import hcyxy.tech.remoting.common.FlexibleReleaseSemaphore
import hcyxy.tech.remoting.entity.Proposal
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ResponseFuture(
    val proposalId: Long,
    val timeout: Long,
    val callback: InvokeCallback?,
    val once: FlexibleReleaseSemaphore?
) {

    @Volatile
    private var sendRequestOk = true
    @Volatile
    private var cause: Throwable? = null
    @Volatile
    private var proposal: Proposal? = null
    private val countDownLatch = FlexibleCountDownLatch(1)
    private val invokeCallback: InvokeCallback? = null
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

    fun waitResponse(timeoutMillis: Long): Proposal? {
        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS)
        return this.proposal
    }

    fun putResponse(proposal: Proposal?) {
        this.proposal = proposal
    }

    fun executeCallback() {
        if (invokeCallback != null) {
            if (this.callbackOnce.compareAndSet(false, true)) {
                invokeCallback.callback(this)
            }

        }
    }

    fun release() {
        once?.release()
    }
}