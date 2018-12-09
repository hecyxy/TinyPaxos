package test.paxos

import hcyxy.tech.server.PaxosServer


fun main(vararg args: String) {
    PaxosServer(11111,22222).start()
}
