package hcyxy.tech.remoting.entity

enum class RemotingCode(val code: Int) {
    NORMAL(200),
    UNKNOWN_PROCESSOR(404),
    TIMEOUT(405),
    SEND_MESSAGE_FAILED(406)
}