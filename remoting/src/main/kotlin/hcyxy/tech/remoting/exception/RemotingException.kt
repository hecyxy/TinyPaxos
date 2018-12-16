package hcyxy.tech.remoting.exception

import java.lang.Exception

open class RemotingException : Exception {

    constructor(message: String) : super(message)


    constructor(message: String, cause: Throwable?) : super(message, cause)

}