package hcy.xy.base

import hcyxy.tech.core.PaxosServer

fun main(vararg args: String) {
    val path = System.getProperty("user.dir")
    val file = "$path/core/src/test/resources/node3.properties"
    PaxosServer(arrayOf(file)).startPaxosServer()
}