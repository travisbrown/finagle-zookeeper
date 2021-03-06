# finagle-zookeeper

finagle-zookeeper provides basic tools to communicate with a Zookeeper server asynchronously.
Note: this is a Google Summer of Code 2014 project, mentored by Twitter, see [the mailing list](https://groups.google.com/forum/?hl=en#!topic/finaglers/GlLXNOvdSVg) for more details.

## Architecture
### Client
- `Client` is based on Finagle 6 client model.

## Commands

Every request returns a *twitter.util.Future* (see [Effective Scala](http://twitter.github.io/effectivescala/#Concurrency-Futures),
[Finagle documentation](https://twitter.github.io/scala_school/finagle.html#Future) and [Scaladoc](http://twitter.github.io/util/util-core/target/doc/main/api/com/twitter/util/Future.html))

Here is the list of commands supported by version 0.1 :

## Test
See src/test/scala

### Client creation
```scala
  val client = ZooKeeper.newRichClient("127.0.0.1:2181,10.0.0.10:2181,192.168.1.1:2181")
```
- `127.0.0.1:2181,10.0.0.10:2181,192.168.1.1:2181` is a String representing the server list, separated by a comma

### Connection
```scala
val connect = client.connect
    connect onSuccess {
      a =>
        logger.info("Connected to zookeeper server: " + client.adress)
    } onFailure {
      e =>
        logger.severe("Connect Error")
    }
```

### Disconnect
```
client.disconnect
```

### First request
Example of request with sequential composition :
```scala
val res = for {
      acl <- client.getACL("/zookeeper")
      _ <- client.create("/zookeeper/test", "HELLO".getBytes, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)
      _ <- client.exists("/zookeeper/test", true)
      _ <- client.setData("/zookeeper/test", "CHANGE".getBytes, -1)
    } yield (acl)
```

### Create
```scala
val create = client.get.create("/zookeeper/hello", "HELLO".getBytes, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)
```
- `/zookeeper/hello` : String the node that you want to create
- `"HELLO".getBytes` : Array[Byte] the data associated to this node
- `Ids.OPEN_ACL_UNSAFE` : default integrated ACL (world:anyone)
- `CreateMode.EPHEMERAL` : Int the creation mode

Return value `Future[String]` representing the path you have just created

- `CreateMode.PERSISTENT` persistent mode
- `CreateMode.EPHEMERAL` ephemeral mode
- `CreateMode.PERSISTENT_SEQUENTIAL` persistent and sequential mode
- `CreateMode.EPHEMERAL_SEQUENTIAL` ephemeral and sequential mode


### Delete
```scala
_ <- client.delete("/zookeeper/test", -1)
```
- `/zookeeper/test` : String the node that you want to delete
- `-1` : Int corresponding version of your data (-1 if you don't care)

Return value `Future[Unit]`

### Exists
```scala
_ <- client.exists("/zookeeper/test", false)
```
- `/zookeeper/test` : String the node that you want to test
- `false` : Boolean if you want to set a watch or not on this node

Return value `Future[ExistsResponse]` `ExistsResponse(stat: Option[Stat], watch: Option[Watcher])`

### Get ACL
```scala
client.getACL("/zookeeper")
```
- `/zookeeper` : String the node from which you want to retrieve ACL

Return value `Future[GetACLResponse]` `GetACLResponse(acl: Array[ACL], stat: Stat)`

### Set ACL
```scala
client.setACL("/zookeeper/test", Ids.OPEN_ACL_UNSAFE, -1)
```
- `/zookeeper/test` : String the node that you want to set
- `Ids.OPEN_ACL_UNSAFE` : default integrated ACL (world:anyone)
- `-1` : Int corresponding version of your data (-1 if you don't care)

Return value `Future[Stat]`

### Get children
```scala
client.getChildren("/zookeeper", false)
```
- `/zookeeper` : String the node that you want to get
- `false` : Boolean if you want to set a watch on this node

Return value `Future[GetChildrenResponse]` `GetChildrenResponse(children: Seq[String], watch: Option[Watcher])`

### Get children2
```scala
client.getChildren2("/zookeeper", false)
```
- `/zookeeper` : String the node that you want to get
- `false` : Boolean if you want to set a watch on this node

Return value `Future[GetChildren2Response]` `GetChildren2Response(children: Seq[String], stat: Stat, watch: Option[Watcher])`

### Get Data
```scala
client.getData("/zookeeper/test", false)
```
- `/zookeeper/test` : String the node that you want to get
- `false` : Boolean if you want to set a watch on this node

Return value `Future[GetDataResponse]` `GetDataResponse(data: Array[Byte], stat: Stat, watch: Option[Watcher])`

### Set Data
```scala
client.setData("/zookeeper/test", "CHANGE".getBytes, -1)
```
- `/zookeeper/test` : String the node that you want to set
- `"CHANGE".getBytes` : Array[Byte] data that you want to set on this node
- `-1` : Int corresponding version of your data (-1 if you don't care)

Return value `Future[Stat]`

### Sync
```scala
client.sync("/zookeeper")
```
- `/zookeeper` : String the node that you want to sync

Return value `Future[String]`

### Transaction

```scala
val opList = Seq(
      CreateRequest(
        "/zookeeper/hello", "TRANS".getBytes, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL),
      SetDataRequest("/zookeeper/hello", "changing".getBytes, -1),
      DeleteRequest("/zookeeper/hell", -1)
    )
```
A transaction can be composed by one or more OpRequests :
```scala
case class CheckVersionRequest(path: String, version: Int)
case class CreateRequest(
  path: String,
  data: Array[Byte],
  aclList: Seq[ACL],
  createMode: Int
  )
case class Create2Request(
  path: String,
  data: Array[Byte],
  aclList: Seq[ACL],
  createMode: Int
  )
case class DeleteRequest(path: String, version: Int)
case class SetDataRequest(
  path: String,
  data: Array[Byte],
  version: Int
  )
```
Each opRequest will return a response of the same type (expect delete and checkVersion that return an EmptyResponse)