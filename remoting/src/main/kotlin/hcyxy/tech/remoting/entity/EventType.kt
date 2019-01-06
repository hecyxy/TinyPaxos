package hcyxy.tech.remoting.entity

enum class EventType(val index: Int) {
    DEFAULT(1000), PROPOSER(1001), ACCEPTOR(1002), LEARNER(1003), LEADER(1004)
}