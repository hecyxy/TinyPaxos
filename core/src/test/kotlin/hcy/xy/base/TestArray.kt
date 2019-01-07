package hcy.xy.base

import org.testng.annotations.Test
import java.util.concurrent.ArrayBlockingQueue

class TestArray {
    @Test
    fun array(){
        // 成功提交的状态
        val array = ArrayBlockingQueue<Int>(1)
        array.put(20)
        array.put(30)
        array.put(40)
        println(array.size)
        println(array.peek())
    }
}