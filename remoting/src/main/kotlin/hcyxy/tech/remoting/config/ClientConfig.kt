package hcyxy.tech.remoting.config

class ClientConfig {
    var workerThreads = 8
    //加锁时间
    var lockTime = 5000L
    //channel最长等待时间
    var channelWait = 2000L
    //允许的异步连接数
    var permitAsync = 2000
    //允许同步连接数
    var permitOnce = 2000
}