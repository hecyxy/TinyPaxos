package hcyxy.tech.remoting

import hcyxy.tech.remoting.protocol.RemotingMsg

interface RequestProcessor {
    fun processRequest(msg: RemotingMsg): RemotingMsg
}