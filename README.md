#### TinyPaxos
this is a simple implement of paxos,just to enforce personal understanding about it.Here is the first version,which is a implement of basic paxos.
And in order to understand it better,the remoting between servers completely uses sync,so it is very ineffective.
In later version,this will be optimized step by step.

#### remoting
this module encapsulates remoting between server and client using netty.

#### start up
##### server/client config
```json
{
  "server": [
    {
      "id": 1,
      "host": "127.0.0.1",
      "port": 8088
    },
    {
      "id": 2,
      "host": "127.0.0.1",
      "port": 8089
    },
    {
      "id": 3,
      "host": "127.0.0.1",
      "port": 8090
    }
  ],
  "serverId": 1,
  "bossThreads": 1,
  "workerThreads": 8,
  "maxConnection": 10000,
  "publicThreadNum": 6,
  "permitAsync": 2000,
  "permitOnce": 2000
}
```
##### start paxos server
package: core/test server1
```kotlin
fun main(vararg args: String) {
    val path = System.getProperty("user.dir")
    val file = "$path/core/src/test/resources/server1.json"
    PaxosServer(arrayOf(file)).startPaxosServer()
}
```

package: core/test server2
```kotlin
fun main(vararg args: String) {
    val path = System.getProperty("user.dir")
    val file = "$path/core/src/test/resources/server2.json"
    PaxosServer(arrayOf(file)).startPaxosServer()
}
```

package: core/test server3
```kotlin
fun main(vararg args: String) {
    val path = System.getProperty("user.dir")
    val file = "$path/core/src/test/resources/server3.json"
    PaxosServer(arrayOf(file)).startPaxosServer()
}
```

##### start client
package: core/test Client
```kotlin
fun main(vararg args: String) {
    val client = RemotingClientImpl(ClientConfig())
    client.start()
    val value = SubmitValue("heihei")
    val body = value.getByteArray()
    val begin = System.currentTimeMillis()
    val msg =
        RemotingMsg.createRequest(ProcessorCode.PROPOSER.code, "submit value", ProposerEventType.Submit.index, body)
    val result = client.invokeSync("127.0.0.1:8088", msg, 10000)
    println(System.currentTimeMillis() - begin)
    result.getBody()?.let { println(getObject(AcceptProposal::class.java, it)) }

    val value2 = SubmitValue("heihei2")
    val body2 = value2.getByteArray()
    val msg1 = RemotingMsg.createRequest(ProcessorCode.PROPOSER.code, "aaa", ProposerEventType.Submit.index, body2)
    val result2 = client.invokeSync("127.0.0.1:8088", msg1, 10000)
    result2.getBody()?.let { println(getObject(AcceptProposal::class.java, it)) }
}
```

#### Reference resources
[paxos小结](https://hcyxy.tech/2018/11/28/paxos%E5%AD%A6%E4%B9%A0%E5%B0%8F%E7%BB%93/)<br/>
[使用Basic-Paxos协议的日志同步与恢复](http://oceanbase.org.cn/?p=90)



