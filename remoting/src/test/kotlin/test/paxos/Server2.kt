package test.paxos

import hcyxy.tech.server.PaxosServer

fun main(vararg args: String) {
    PaxosServer(22222, 11111).start()
}