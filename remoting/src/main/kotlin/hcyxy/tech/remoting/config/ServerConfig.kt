package hcyxy.tech.remoting.config

class ServerConfig {
    var port = 8888
    var bossThreads = 1
    var workerThreads = 8
    var maxConnection = 2000
    var publicThreadNum = 6
    //允许的异步连接数
    var permitAsync = 2000
    //允许同步连接数
    var permitOnce = 2000
}