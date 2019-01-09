package test.paxos

import java.util.concurrent.atomic.AtomicLong

class A {
    companion object {
        private val increment = AtomicLong(0)
        var bb = 23
    }

    var a: Long = increment.incrementAndGet()
}

fun main(vararg args: String) {
    val a = A()
    println(a.a)
    println(a.a)
    A.bb = 25
    val b = A()
    println(b.a)
    println(b.a)
    println(A.bb)
}
