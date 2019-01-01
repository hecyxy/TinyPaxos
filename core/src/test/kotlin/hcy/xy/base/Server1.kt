package hcy.xy.base

import hcyxy.tech.core.PaxosServer

fun main(vararg args: String) {
    PaxosServer("resource/node1.properties").startPaxosServer()
}