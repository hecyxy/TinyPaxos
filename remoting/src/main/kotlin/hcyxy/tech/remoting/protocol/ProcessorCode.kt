package hcyxy.tech.remoting.protocol

enum class ProcessorCode(val code: Int) {
    DEFAULT(1000), PROPOSER(1001), ACCEPTOR(1002), LEARNER(1003), LEADER(1004)
}