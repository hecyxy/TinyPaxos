package hcyxy.tech.remoting.protocol

enum class RemotingCode(val code: Int) {
    NORMAL(200),
    UNKNOWN_PROCESSOR(404),
    TIMEOUT(405),
    SEND_MESSAGE_FAILED(406),
    UNKNOWN_PACKET(407)
}