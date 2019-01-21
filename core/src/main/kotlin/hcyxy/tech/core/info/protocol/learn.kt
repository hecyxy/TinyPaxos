package hcyxy.tech.core.info.protocol

data class LearnRequest(var logId: Long)

data class LearnResponse(var logId: Long, var value: String)