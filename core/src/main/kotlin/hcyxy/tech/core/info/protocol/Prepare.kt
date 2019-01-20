package hcyxy.tech.core.info.protocol

data class PrepareRequest(var logId: Long = 0, var proposalId: Long = 0)

data class PrepareResponse(
    var logId: Long = 0,
    var proposalId: Long = 0,
    var accept: Boolean,
    var content: AcceptProposal?
)