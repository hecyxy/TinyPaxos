package test.paxos

abstract class Base {
    var code = calculate()
    abstract fun calculate(): Int
}

class Derived(private val x: Int) : Base() {
    override fun calculate() = x
}

fun main(args: Array<String>) {
    println(Derived(42).code) // Expected: 42, actual: 0
}