package hcyxy.tech.remoting

import hcyxy.tech.remoting.entity.Proposal

interface RequestProcessor {
    fun processRequest(proposal: Proposal): Proposal
}