package hcyxy.tech.core

import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.client.RemotingClientImpl
import hcyxy.tech.remoting.config.ClientConfig
import hcyxy.tech.remoting.config.ServerConfig
import hcyxy.tech.remoting.server.RemotingServer
import hcyxy.tech.remoting.server.RemotingServerImpl
import org.slf4j.LoggerFactory
import java.lang.Exception

class PaxosServer {
    private val logger = LoggerFactory.getLogger(PaxosServer::class.java)

    fun startPaxosServer() {
        try {
            val serverConfig = ServerConfig()
            val clientConfig = ClientConfig()
            val controller = ServerController(serverConfig, clientConfig)
            controller.start()
            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    controller.shutdown()
                }
            })
        } catch (e: Exception) {
            logger.error("start paxos server failed")
        }
    }

    internal class ServerController(private val serverConfig: ServerConfig, private val clientConfig: ClientConfig) {
        private var remotingServer: RemotingServer? = null

        private var remotingCient: RemotingClient? = null

        private fun inialize() {
            remotingCient = RemotingClientImpl(clientConfig)
            remotingServer = RemotingServerImpl(serverConfig)
        }

        fun start() {
            inialize()

        }

        fun shutdown() {

        }
    }
}