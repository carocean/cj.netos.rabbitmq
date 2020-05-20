# rabbitmq内嵌包

* 生产包供钱包和纹银银行等网关节点使用
* 消息包供钱包和纹银银行等交易节点使用

## 使用
  放入项目的内嵌包中
  
## 生产者配置
```yaml
host: 47.104.128.137
port: 5675
virtualHost: /claf/wybank
user: superadmin
pwd: 123456
#abbitmq 自带恢复连接机制，只要为true
automaticRecoveryEnabled: true
#心跳时间，单位为秒，0表示不用
requestedHeartbeat: 0
# 连接超时时间，单位毫秒，0表示不限定
connectionTimeout: 0
# 连接池超时时间，单位毫秒，0表示不限定
workPoolTimeout: 0
exchange:
  # 该交换器名提供给消费者绑定
  name: wybank.trade.amq.direct
  # direct|fanout|topic|header
  type: direct
  #设置是否持久 durable 设置为 true 表示持久化， 反之是非持久,设置为true则将Exchange存盘，即使服务器重启数据也不会丢失。默认为false
  durable: true
  #设置是否自动删除，当最后一个绑定到Exchange上的队列删除后，自动删除该Exchange，简单来说也就是如果该Exchange没有和任何队列Queue绑定则删除，默认为false
  autoDelete: false
  #设置是否是RabbitMQ内部使用，默认false。如果设置为 true ，则表示是内置的交换器，客户端程序无法直接发送消息到这个交换器中，只能通过交换器路由到交换器这种方式。
  internal: false
  #扩展参数，用于扩展AMQP协议自制定化使用
  arguments:
# 当 exchange.type=fanout|header 时routing配置被忽略
routing:
  # 传播模式，该配置项仅在仅在exchange的type=direct有效。
  # unicast 单播。为默认
  # multicast 多播。该功能等效于type=fanout，建议多播使用type=fanout
  directCast: unicast
  #为true时，如果exchange根据自身类型和消息routingKey无法找到一个合适的queue存储消息，那么broker会调用basic.return方法将消息返还给生产者;当mandatory设置为false时，出现上述情况broker会直接将消息丢弃;通俗的讲，mandatory标志告诉broker代理服务器至少将消息route到一个队列中，否则就将消息return给发送者;
  #默认为true
  mandatory: true
  #基本与mandatory作用相似。
  #在RabbitMQ3.0以后的版本里，去掉了immediate参数的支持，发送带immediate标记的publish会返回如下错误：
  #“{amqp_error,not_implemented,“immediate=true”,‘basic.publish’}”
  #immediate标记会影响镜像队列性能，增加代码复杂性
  #默认为false
  #immediate: false

  # 当 exchange.type=topic 时支持通配符
  # 路由到相应的消费者队列，在netos.rabbitmq下，每个消费者自动创建一个队列，并绑定一个routingKey以接收
  routingKeys:
    - wybank.ec1.trade
```

#消费者配置
```yaml
host: 47.104.128.137
port: 5675
virtualHost: /claf/wybank
user: superadmin
pwd: 123456
#abbitmq 自带恢复连接机制，只要为true
automaticRecoveryEnabled: true
#心跳时间，单位为秒，0表示不用
requestedHeartbeat: 0
# 连接超时时间，单位毫秒，0表示不限定
connectionTimeout: 0
# 连接池超时时间，单位毫秒，0表示不限定
workPoolTimeout: 0
queue:
  #队列名。该队列会自动在rabbitmq上创建，如果存在则可直接使用
  name: wybank.ec1.trade
  #设置是否持久化。为 true 则设置队列为持久化。持久化的队列会存盘，在 服务器重启的时候可以保证不丢失相关信息。
  #默认为false
  durable: true
  #设置是否自动删除。为 true 则设置队列为自动删除。自动删除的前提是: 至少有一个消费者连接到这个队列，之后所有与这个队列连接的消费者都断开时，才会 自动删除。不能把这个参数错误地理解为: "当连接到此队列的所有客户端断开时，这 个队列自动删除"，因为生产者客户端创建这个队列，或者没有消费者客户端与这个队 列连接时，都不会自动删除这个队列。
  #默认为true
  autoDelete: false
  #设置是否排他。为 true 则设置队列为排他的。如果一个队列被声明为排 他队列，该队列仅对首次声明它的连接可见，并在连接断开时自动删除。这里需要注意 三点:排他队列是基于连接( Connection) 可见的，同 个连接的不同信道 (Channel) 是可以同时访问同一连接创建的排他队列; "首次"是指如果 个连接己经声明了 排他队列，其他连接是不允许建立同名的排他队列的，这个与普通队列不同:即使该队 列是持久化的，一旦连接关闭或者客户端退出，该排他队列都会被自动删除，这种队列 适用于一个客户端同时发送和读取消息的应用场景。
  #默认为false
  exclusive: false
  #设置队列的其他一些参数，如 x-rnessage-ttl 、x-expires 、x-rnax-length 、x-rnax-length-bytes、 x-dead-letter-exchange、 x-deadletter-routing-key 、 x-rnax-priority 等。
  arguments:
#仅接收来自exchanges配置的routingKey的消息。如果exchange是的type=fanout则忽略routingKey直接接收
routingKey: wybank.ec1.trade
#当自动应答等于true的时候，表示当消费者一收到消息就表示消费者收到了消息，消费者收到了消息就会立即从队列中删除。
#消息的自动ACK确认机制默认是true
#在非自动确认模式下如果忘记了ACK，那么后果很严重。当Consumer退出时候，Message会一直重新分发
#可以修改rabbitmq的配置以设定重试次数
autoAck: false
#RabbitMQ提供了一种qos（服务质量保证，即流量控制）功能，即在非自动确认消息的前提下，如果一定数目的消息（通过基于consume或者channel设置Qos的值）未被确认前，不进行消费新的消息。
basicQos:
  #限制消息本身的大小
  #0为无限制
  prefetchSize: 0
  #会告诉RabbitMQ不要同时给一个消费者推送多于N个消息，即一旦有N个消息还没有ack，则该consumer将等待，直到有消息ack
  #0为无限制
  prefetchCount: 10
  #true是将上面设置应用于channel，简单点说，就是上面限制是channel级别的还是consumer级别
  #默认为false
  global: false
# 可以绑定到多个exchange上接收消息
#使用的交换器必须在rabbitmq上已经创建。注：如果先启动生产者节点会自动创建，也可按生产者节点的交换器配置手动在rabbitmq上配置
exchanges:
  - wybank.trade.amq.direct
#如果exchanges列表中的交换机在服务器上不存在，则在此列表中查找并自动创建。
#由于生产会自动创建交换机，而消费者只是绑定到交换机上使用，这导致总是要先启动生产节点而后再启动消费节点。但如果一个节点即是消费节点又是生产节点，则节点便产生异常，致使必须手动在rabbitmq上创建交换机，因此：
#autoCreateExchanges配置可以在消费者节点检测到不存在要绑定的交换机时自动创建交换机。但是如果与生产者节点配置的交换机属性不同的话会报出异常，因此：
#下面的配置信息要照抄生产节点相应的交换机配置且要一模一样
autoCreateExchanges:
  - # 该交换器名提供给消费者绑定
    name: wybank.trade.amq.direct
    # direct|fanout|topic|header
    type: direct
    #设置是否持久 durable 设置为 true 表示持久化， 反之是非持久,设置为true则将Exchange存盘，即使服务器重启数据也不会丢失。默认为false
    durable: true
    #设置是否自动删除，当最后一个绑定到Exchange上的队列删除后，自动删除该Exchange，简单来说也就是如果该Exchange没有和任何队列Queue绑定则删除，默认为false
    autoDelete: false
    #设置是否是RabbitMQ内部使用，默认false。如果设置为 true ，则表示是内置的交换器，客户端程序无法直接发送消息到这个交换器中，只能通过交换器路由到交换器这种方式。
    internal: false
    #扩展参数，用于扩展AMQP协议自制定化使用
    arguments:
```