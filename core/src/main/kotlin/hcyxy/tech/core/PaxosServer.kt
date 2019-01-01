package hcyxy.tech.core

import hcyxy.tech.core.common.ThreadFactoryImpl
import hcyxy.tech.core.processor.AcceptorProcessor
import hcyxy.tech.core.processor.LearnerProcessor
import hcyxy.tech.core.processor.ProposerProcessor
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.client.RemotingClientImpl
import hcyxy.tech.remoting.config.ClientConfig
import hcyxy.tech.remoting.config.ServerConfig
import hcyxy.tech.remoting.entity.EventType
import hcyxy.tech.remoting.server.RemotingServer
import hcyxy.tech.remoting.server.RemotingServerImpl
import org.slf4j.LoggerFactory
import java.util.concurrent.*

class PaxosServer {
    private val logger = LoggerFactory.getLogger(PaxosServer::class.java)
    private var publicExecutor: ExecutorService? = null
    private var path: String? = null

    constructor(param: String?) {
        this.path = param
    }

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
            logger.error("start processor server failed")
        }
    }

    internal inner class ServerController(
        private val serverConfig: ServerConfig,
        private val clientConfig: ClientConfig
    ) {
        private var remotingServer: RemotingServer? = null

        private var remotingCient: RemotingClient? = null

        private var blockingQueue: BlockingQueue<Runnable>? = null
        private fun initialize() {
            blockingQueue = LinkedBlockingQueue<Runnable>(1024 * 10)
            remotingCient = RemotingClientImpl(clientConfig)
            remotingServer = RemotingServerImpl(serverConfig)
            publicExecutor = ThreadPoolExecutor(
                8, 8,
                1000 * 60, //
                TimeUnit.MILLISECONDS, //
                blockingQueue,
                ThreadFactoryImpl("PublicExecutor")
            )
            val proposerProcessor = ProposerProcessor(remotingCient)
            remotingServer?.registerProcessor(EventType.PROPOSER.index, proposerProcessor, publicExecutor)
            val acceptorProcessor = AcceptorProcessor(remotingCient)
            remotingServer?.registerProcessor(EventType.ACCEPTOR.index, acceptorProcessor, publicExecutor)
            val learnerProcessor = LearnerProcessor(remotingCient)
            remotingServer?.registerProcessor(EventType.LEARNER.index, learnerProcessor, publicExecutor)
        }

        fun start() {
            initialize()
            this.remotingServer?.start()
            this.remotingCient?.start()

        }

        fun shutdown() {
            this.remotingCient?.shutdown()
            this.remotingServer?.shutdown()
            this.blockingQueue?.clear()
        }
    }
}