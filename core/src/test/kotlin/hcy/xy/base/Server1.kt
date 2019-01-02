package hcy.xy.base

import hcyxy.tech.core.PaxosServer
import java.io.FileInputStream
import java.util.*


fun main(vararg args: String) {
    val path = System.getProperty("user.dir")
    val pro = Properties()
    val `in` = FileInputStream("$path/core/src/test/resources/node1.properties")
    val file = "$path/core/src/test/resources/node1.properties"
    pro.load(`in`)
    val server = pro.getProperty("jfj")
    PaxosServer(arrayOf(file)).startPaxosServer()
}