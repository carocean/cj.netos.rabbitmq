package cj.netos.rabbitmq.consumer;

import cj.netos.rabbitmq.*;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import com.rabbitmq.client.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@CjService(name = "rabbitMQConsumer")
public class RabbitMQConsumer implements IRabbitMQConsumer {
    RabbitMQConsumerConfig config;
    Channel channel;
    Connection connection;
    File confFile;
    ReentrantLock lock;
    Condition pauseController;


    @Override
    public boolean isOpened() {
        return (channel != null && channel.isOpen());
    }


    @Override
    public RabbitMQConsumerConfig config() {
        return config;
    }

    @Override
    public Channel innerOpen() throws RabbitMQException {
        return open(confFile);
    }

    @Override
    public Channel open(File confFile) throws RabbitMQException {
        this.confFile = confFile;
        lock = new ReentrantLock();
        pauseController = lock.newCondition();

        _loadConfig();

        String host = config.getHost();
        int port = config.getPort();
        String virtualHost = config.getVirtualHost();
        String user = config.getUser();
        String pwd = config.getPwd();
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try {
            connectionFactory.setHost(host);
            connectionFactory.setPort(port);
            connectionFactory.setVirtualHost(virtualHost);
            connectionFactory.setUsername(user);
            connectionFactory.setPassword(pwd);
            connectionFactory.setAutomaticRecoveryEnabled(config.isAutomaticRecoveryEnabled());
            if (config.getRequestedHeartbeat() > 0) {
                connectionFactory.setRequestedHeartbeat(config.getRequestedHeartbeat());
            }
            if (config.getConnectionTimeout() > 0) {
                connectionFactory.setConnectionTimeout(config.getConnectionTimeout());
            }
            if (config.getWorkPoolTimeout() > 0) {
                connectionFactory.setWorkPoolTimeout(config.getWorkPoolTimeout());
            }

            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
            //设置客户端最多接收未被ack的消息的个数
            BasicQosConfig basicQosConfig = config.getBasicQos();
            channel.basicQos(basicQosConfig.getPrefetchSize(), basicQosConfig.getPrefetchCount(), basicQosConfig.isGlobal());
            //4 声明交换机和队列，然后进行绑定设置路由Key
            QueueConfig queueConfig = config.getQueue();
            channel.queueDeclare(queueConfig.getName(), queueConfig.isDurable(), queueConfig.isExclusive(), queueConfig.isAutoDelete(), queueConfig.getArguments());
            for (String exchange : config.getExchanges()) {
                Map<String, ExchangeConfig> exchangeConfigMap = config.getAutoCreateExchanges();
                if (exchangeConfigMap.containsKey(exchange)) {
                    ExchangeConfig exchangeConfig = exchangeConfigMap.get(exchange);
                    channel.exchangeDeclare(exchange, exchangeConfig.getType(), exchangeConfig.isDurable(), exchangeConfig.isAutoDelete(), exchangeConfig.isInternal(), exchangeConfig.getArguments());
                    channel.queueBind(queueConfig.getName(), exchange, config.getRoutingKey());
                    continue;
                }
                //如果已存在则使用该方法，而使用exchangeDeclare这个方法，如果存在则直接用不存在则会自建。
                //经验证，一个channel上可以定义多个交换器，但如果一个交换器已被定义，则不可再通过exchangeDeclare修改，那怕是变更属性都不可
                try {
                    channel.exchangeDeclarePassive(exchange);
                    channel.queueBind(queueConfig.getName(), exchange, config.getRoutingKey());
                } catch (IOException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof ShutdownSignalException) {
                        ShutdownSignalException shutdownSignalException = (ShutdownSignalException) cause;
                        AMQP.Channel.Close method = (AMQP.Channel.Close) shutdownSignalException.getReason();
                        int replyCode = method.getReplyCode();
                        if (replyCode == 404) {
                            throw new RabbitMQException("404", String.format("RabbitMQ上不存在交换器 %s，请先启动消息生产者节点以生成Exchange，或者按生产者节点的交换机配置手工在rabbitmq上配置该交换器", exchange));
                        }
                    }
                    throw new RabbitMQException("500", e);
                }
            }
            CJSystem.logging().info(getClass(), "连接mq成功，配置如下:");
            config.printLog();
            return channel;
        } catch (TimeoutException e) {
            throw new RabbitMQException("500", e);
        } catch (IOException e) {
            throw new RabbitMQException("500", e);
        }
    }

    private void _loadConfig() throws RabbitMQException {
        Reader reader = null;
        try {
            reader = new FileReader(confFile);
            config = RabbitMQConsumerConfig.load(reader);
        } catch (IOException e) {
            throw new RabbitMQException("500", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public void close() throws RabbitMQException {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                CJSystem.logging().error(getClass(), e);
            } catch (TimeoutException e) {
                CJSystem.logging().error(getClass(), e);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                CJSystem.logging().error(getClass(), e);
            }
        }
        CJSystem.logging().info(getClass(), String.format("已断开mq"));
    }

    @Override
    public void acceptConsumer(IConsumer consumer) throws RabbitMQException {
        //5 设置channel，使用自定义消费者
        QueueConfig queueConfig = config.getQueue();
        try {
            channel.basicConsume(queueConfig.getName(), config.isAutoAck(), new CLAFConsumerDelivery(channel, consumer));
        } catch (IOException e) {
            throw new RabbitMQException("500", e);
        }
    }
}

class CLAFConsumerDelivery extends DefaultConsumer {
    IConsumer consumer;

    public CLAFConsumerDelivery(Channel channel, IConsumer consumer) {
        super(channel);
        this.consumer = consumer;
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        consumer.handleShutdownSignal(consumerTag, sig);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        try {
            consumer.handleDelivery(getChannel(), consumerTag, envelope, properties, body);
            //如果发生错误则会导致channel并闭，因此捕获
        } catch (Throwable throwable) {
            CJSystem.logging().error(getClass(), throwable);
            //默认异常则会消费掉消息（即丢掉）
            getChannel().basicReject(envelope.getDeliveryTag(), false);
        }
    }
}
