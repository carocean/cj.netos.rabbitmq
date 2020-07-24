package cj.netos.rabbitmq.producer;

import cj.netos.rabbitmq.*;
import cj.netos.rabbitmq.util.Encript;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.util.StringUtil;
import com.rabbitmq.client.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@CjService(name = "rabbitMQProducer")
public class RabbitMQProducer implements IRabbitMQProducer, ReturnListener {
    RabbitMQProducerConfig config;
    Channel channel;
    Connection connection;
    File confFile;
    ReentrantLock lock;
    Condition pauseController;
    AtomicBoolean pause = new AtomicBoolean(false);

    @Override
    public boolean isOpened() {
        return channel.isOpen() && connection.isOpen();
    }

    @Override
    public boolean isPausing() {
        return pause.get();
    }

    @Override
    public RabbitMQProducerConfig config() {
        return config;
    }


    protected synchronized String selectRouteKey(RoutingNode routingNode, String factor) throws CircuitException {
        List<String> keys = routingNode.getRoutingKeys();
        if (keys.isEmpty()) {
            return null;
        }
        String sha1 = Encript.sha1(factor);
        int hash = Math.abs(sha1.hashCode());
        int index = hash % keys.size();
        return keys.get(index);
    }

    @Override
    public void reopen() throws CircuitException {
        if (channel.isOpen()) {
            close();
        }
        open(confFile);
    }

    @Override
    public Channel open(File confFile) throws CircuitException {
        this.confFile = confFile;
        lock = new ReentrantLock();
        pauseController = lock.newCondition();

        _loadConfig();

        String host = config.getHost();
        int port = config.getPort();
        String virtualHost = config.getVirtualHost();
        String user = config.getUser();
        String pwd = config.getPwd();
        ExchangeConfig exchange = config.getExchange();
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
            channel.exchangeDeclare(exchange.getName(), exchange.getType(), exchange.isDurable(), exchange.isAutoDelete(), exchange.isInternal(), exchange.getArguments());
            channel.addReturnListener(this);
            CJSystem.logging().info(getClass(), "连接mq成功，配置如下:");
            config.printLog();
            return channel;
        } catch (TimeoutException e) {
            throw new CircuitException("500", e);
        } catch (IOException e) {
            throw new CircuitException("500", e);
        }
    }

    @Override
    public void refreshConfig() throws CircuitException {
        pause.set(true);
        try {
            lock.lock();
            _loadConfig();
            pauseController.signalAll();
        } catch (Exception e) {
            throw new CircuitException("500", e);
        } finally {
            pause.set(false);
            lock.unlock();
        }
    }

    private void _loadConfig() throws CircuitException {
        Reader reader = null;
        try {
            reader = new FileReader(confFile);
            config = RabbitMQProducerConfig.load(reader);
        } catch (IOException e) {
            throw new CircuitException("500", e);
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
    public void close() throws CircuitException {
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
    public void publish(String sendToRoutingNode, AMQP.BasicProperties props, byte[] body) throws CircuitException {
        if (pause.get()) {
            CJSystem.logging().info(getClass(), "暂停发送消息，正在重新加载Config...");
            try {
                lock.lock();
                pauseController.await(15000L, TimeUnit.MILLISECONDS);
                CJSystem.logging().info(getClass(), "完成routingKeys列表的更新，继续发送消息");
            } catch (InterruptedException e) {
                CJSystem.logging().warn(getClass(), String.format("等待更新routingKeys列表超时"));
            } finally {
                lock.unlock();
            }
        }

        RoutingConfig routingConfig = config.getRouting();
        //fanout模式无路由键，因此直接发送
        if ("fanout".equals(config.getExchange().getType())) {
            if (!StringUtil.isEmpty(sendToRoutingNode)) {
                CJSystem.logging().warn(getClass(), String.format("交换器类型是fanout，指定发送目标没有意义。忽略配置目标并发送"));
                return;
            }
            try {
                channel.basicPublish(config.getExchange().getName(), null, routingConfig.isMandatory(), routingConfig.isImmediate(), props, body);
                return;
            } catch (IOException e) {
                throw new CircuitException("500", e);
            }
        }

        if (StringUtil.isEmpty(sendToRoutingNode)) {
            CJSystem.logging().warn(getClass(), String.format("发送目录为空，已忽略发送此消息"));
            return;
        }
        /*
        1. mandatory标志位
        当mandatory标志位设置为true时，如果exchange根据自身类型和消息routeKey无法找到一个符合条件的queue，那么会调用basic.return方法将消息返还给生产者；当mandatory设为false时，出现上述情形broker会直接将消息扔掉。

        2. immediate标志位
        当immediate标志位设置为true时，如果exchange在将消息route到queue(s)时发现对应的queue上没有消费者，那么这条消息不会放入队列中。当与消息routeKey关联的所有queue(一个或多个)都没有消费者时，该消息会通过basic.return方法返还给生产者。
       */

        Map<String, RoutingNode> routingNodeMap = routingConfig.getRoutingNodes();
        RoutingNode routingNode = routingNodeMap.get(sendToRoutingNode);
        if (routingNode == null) {
            CJSystem.logging().warn(getClass(), String.format("未知的路由节点:%s，已忽略发送此消息", sendToRoutingNode));
            return;
        }
        if (routingNode.getCastmode() == null) {
            routingNode.setCastmode(Castmode.unicast);
        }
        switch (routingNode.getCastmode()) {
            case multicast:
                List<String> keys = routingNode.getRoutingKeys();
                for (String routingKey : keys) {
                    try {
                        channel.basicPublish(config.getExchange().getName(), routingKey, routingConfig.isMandatory(), routingConfig.isImmediate(), props, body);
                    } catch (IOException e) {
                        throw new CircuitException("500", e);
                    }
                }
                break;
            case unicast:
                String routingKey = selectRouteKey(routingNode, body.hashCode() + UUID.randomUUID().toString());
                try {
                    channel.basicPublish(config.getExchange().getName(), routingKey, routingConfig.isMandatory(), routingConfig.isImmediate(), props, body);
                } catch (IOException e) {
                    throw new CircuitException("500", e);
                }
                break;
        }

    }

    @Override
    public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
        CJSystem.logging().warn(getClass(), String.format("消息被丢弃，replyCode=%s, replyText=%s 请检查routingKey是否与交易节点注册的routingKey对应，详情:", replyCode, replyText));
        CJSystem.logging().warn(getClass(), String.format("\texchange:%s", exchange));
        CJSystem.logging().warn(getClass(), String.format("\troutingKey:%s", routingKey));
        CJSystem.logging().warn(getClass(), String.format("\tbody:\r\n%s", new String(body)));
    }
}
