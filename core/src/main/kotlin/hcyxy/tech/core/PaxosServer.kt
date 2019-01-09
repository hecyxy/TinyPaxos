package hcyxy.tech.core

import hcyxy.tech.core.constants.PaxosConfig
import hcyxy.tech.core.processor.AcceptorProcessor
import hcyxy.tech.core.processor.LearnerProcessor
import hcyxy.tech.core.processor.ProposerProcessor
import hcyxy.tech.core.service.ThreadFactoryImpl
import hcyxy.tech.core.util.FileUtil
import hcyxy.tech.core.util.notnull
import hcyxy.tech.remoting.client.RemotingClient
import hcyxy.tech.remoting.client.RemotingClientImpl
import hcyxy.tech.remoting.config.ClientConfig
import hcyxy.tech.remoting.config.ServerConfig
import hcyxy.tech.remoting.protocol.ProcessorCode
import hcyxy.tech.remoting.server.RemotingServer
import hcyxy.tech.remoting.server.RemotingServerImpl
import org.slf4j.LoggerFactory
import java.util.concurrent.*

class PaxosServer(private val param: Array<String>) {

    private val logger = LoggerFactory.getLogger(PaxosServer::class.java)
    private var publicExecutor: ExecutorService? = null
    private var paxosConfig: PaxosConfig? = null
    fun startPaxosServer() {
        try {
            if (param.size == 1) {
                paxosConfig = FileUtil.file2Json(param[0])
            } else {
                throw Exception("缺失参数")
            }
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

        val serverConfig = ServerConfig()
        try {
            serverConfig.port = getServerPort()
            paxosConfig?.bossThreads?.let { serverConfig.bossThreads = it }
            paxosConfig?.maxConnection?.let { serverConfig.maxConnection = it }
            paxosConfig?.publicThreadNum?.let { serverConfig.publicThreadNum = it }
        } catch (e: Exception) {
            logger.warn("parse param failed", e)
        }
        return serverConfig
    }

    private fun parseClientParam(): ClientConfig {
        val serverConfig = ClientConfig()
        try {
            paxosConfig?.workerThreads?.let { serverConfig.workerThreads = it }
            paxosConfig?.lockTime?.let { serverConfig.lockTime = it }
            paxosConfig?.channelWait?.let { serverConfig.channelWait = it }
            paxosConfig?.permitAsync?.let { serverConfig.permitAsync = it }
        } catch (e: Exception) {
            logger.warn("parse param failed", e)
        }
        return serverConfig
    }

    private fun getServerPort(): Int {
        val serverList = notnull(paxosConfig?.server, "配置信息错误")
        val node = serverList.associateBy { it.id }
        val serverId = notnull(paxosConfig?.serverId, "配置信息错误")
        val port = notnull(node[serverId]?.port, "配置信息错误")
        return port
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
            val serverId = notnull(paxosConfig?.serverId, "配置信息错误")
            val serverList = notnull(paxosConfig?.server, "配置信息错误")
            val acceptorProcessor = AcceptorProcessor(serverId, serverList, client)
            remotingServer?.registerProcessor(ProcessorCode.ACCEPTOR.code, acceptorProcessor, publicExecutor)
            val proposerProcessor = ProposerProcessor(serverId, serverList, client, acceptorProcessor)
            remotingServer?.registerProcessor(ProcessorCode.PROPOSER.code, proposerProcessor, publicExecutor)
            val learnerProcessor = LearnerProcessor(serverId, serverList, client)
            remotingServer?.registerProcessor(ProcessorCode.LEARNER.code, learnerProcessor, publicExecutor)
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