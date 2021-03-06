package hcy.xy.base

import hcyxy.tech.core.PaxosServer


fun main(vararg args: String) {
    val path = System.getProperty("user.dir")
    val file = "$path/core/src/test/resources/server1.json"
    PaxosServer(arrayOf(file)).startPaxosServer()
}