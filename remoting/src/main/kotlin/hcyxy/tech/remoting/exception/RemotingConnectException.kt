package hcyxy.tech.remoting.exception

class RemotingConnectException : RemotingException {

    constructor(addr: String) : this(addr, null)

    constructor(addr: String, cause: Throwable?) : super("connect $addr failed", cause)
}