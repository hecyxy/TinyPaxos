package hcyxy.tech.core

import hcyxy.tech.core.service.ThreadFactoryImpl
import hcyxy.tech.core.processor.AcceptorProcessor
import hcyxy.tech.core.processor.LearnerProcessor
import hcyxy.tech.core.processor.ProposerProcessor
import hcyxy.tech.core.service.notnull
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.client.RemotingClientImpl
import hcyxy.tech.remoting.config.ClientConfig
import hcyxy.tech.remoting.config.ServerConfig
import hcyxy.tech.remoting.entity.EventType
import hcyxy.tech.remoting.server.RemotingServer
import hcyxy.tech.remoting.server.RemotingServerImpl
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.*

class PaxosServer(private val param: Array<String>) {

    private val logger = LoggerFactory.getLogger(PaxosServer::class.java)
    private var publicExecutor: ExecutorService? = null
    private var machineId: Int = 1
    private
    fun startPaxosServer() {
        try {
            val serverConfig = parseServerParam()
            val clientConfig = parseClientParam()
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

    private fun parseServerParam(): ServerConfig {
        var input: FileInputStream? = null
        val serverConfig = ServerConfig()
        try {
            if (param[0].isNotBlank()) {
                val pro = Properties()
                input = FileInputStream(param[0])
                pro.load(input)
                pro.getProperty("port")?.let { serverConfig.port = it.toInt() }
                pro.getProperty("bossThreads")?.let { serverConfig.bossThreads = it.toInt() }
                pro.getProperty("maxConnection")?.let { serverConfig.maxConnection = it.toInt() }
                pro.getProperty("publicThreadNum")?.let { serverConfig.publicThreadNum = it.toInt() }
                pro.getProperty("machineId")?.let { machineId = it.toInt() }
            }
        } catch (e: Exception) {
            logger.warn("parse param failed", e)
        } finally {
            input?.let { it.close() }
        }
        return serverConfig
    }

    private fun parseClientParam(): ClientConfig {
        var input: FileInputStream? = null
        val serverConfig = ClientConfig()
        try {
            if (param[0].isNotBlank()) {
                val pro = Properties()
                input = FileInputStream(param[0])
                pro.load(input)
                pro.getProperty("workerThreads")?.let { serverConfig.workerThreads = it.toInt() }
                pro.getProperty("lockTime")?.let { serverConfig.lockTime = it.toLong() }
                pro.getProperty("channelWait")?.let { serverConfig.channelWait = it.toLong() }
                pro.getProperty("permitAsync")?.let { serverConfig.permitAsync = it.toInt() }
            }
        } catch (e: Exception) {
            logger.warn("parse param failed", e)
        } finally {
            input?.let { it.close() }
        }
        return serverConfig
    }

    internal inner class ServerController(
        private val serverConfig: ServerConfig,
        private val clientConfig: ClientConfig
    ) {
        private var remotingServer: RemotingServer? = null

        private var remotingClient: RemotingClient? = null

        private var blockingQueue: BlockingQueue<Runnable>? = null
        private fun initialize() {
            blockingQueue = LinkedBlockingQueue<Runnable>(1024 * 10)
            remotingClient = RemotingClientImpl(clientConfig)
            remotingServer = RemotingServerImpl(serverConfig)
            publicExecutor = ThreadPoolExecutor(
                8, 8,
                1000 * 60,
                TimeUnit.MILLISECONDS,
                blockingQueue,
                ThreadFactoryImpl("PublicExecutor")
            )
            val client = notnull(remotingClient, "client启动异常")
            val proposerProcessor = ProposerProcessor(client, machineId)
            remotingServer?.registerProcessor(EventType.PROPOSER.index, proposerProcessor, publicExecutor)
            val acceptorProcessor = AcceptorProcessor(client, machineId)
            remotingServer?.registerProcessor(EventType.ACCEPTOR.index, acceptorProcessor, publicExecutor)
            val learnerProcessor = LearnerProcessor(client, machineId)
            remotingServer?.registerProcessor(EventType.LEARNER.index, learnerProcessor, publicExecutor)
        }

        fun start() {
            initialize()
            this.remotingServer?.start()
            this.remotingClient?.start()

        }

        fun shutdown() {
            this.remotingClient?.shutdown()
            this.remotingServer?.shutdown()
            this.blockingQueue?.clear()
        }
    }
}