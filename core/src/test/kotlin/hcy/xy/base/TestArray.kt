package hcy.xy.base

import org.testng.annotations.Test
import java.util.concurrent.ArrayBlockingQueue

class TestArray {
    @Test
    fun array() {
        // 成功提交的状态
        val array = ArrayBlockingQueue<Int>(1)
        array.put(20)
        println(array.peek())
        println(array.peek())

        println(array.peek())
    }

    @Test
    fun testMax() {
        data class A(var id: Long, var content: String?)
        val list = mutableListOf<A>()
        list.add(A(1,null))
        list.add(A(2,"4"))
        list.add(A(3,"5"))
        list.filter { it.content!=null }.sortedByDescending  { it.id }.forEach {
            println(it.id)
            println(it.content)
        }
    }
}