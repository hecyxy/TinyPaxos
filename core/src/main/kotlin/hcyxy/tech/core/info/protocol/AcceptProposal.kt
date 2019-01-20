package hcyxy.tech.core.info.protocol

data class AcceptProposal(var logId: Long, var proposalId: Long, var value: String)

data class AcceptRequest(var logId: Long, var proposalId: Long, var content: String)

data class AcceptResponse(var accept: Boolean)