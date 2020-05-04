package cj.netos.rabbitmq.producer;

import cj.netos.rabbitmq.IRabbitMQProducer;
import cj.netos.rabbitmq.RabbitMQProducerConfig;
import cj.netos.rabbitmq.util.Encript;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import com.rabbitmq.client.*;

import java.io.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@CjService(name = "rabbitMQProducer")
public class RabbitMQProducer implements IRabbitMQProducer, ReturnListener {
    private boolean isOpened;
    RabbitMQProducerConfig config;
    Channel channel;
    Connection connection;
    File confFile;
    ReentrantLock lock;
    Condition pauseController;
    AtomicBoolean pause = new AtomicBoolean(false);
    private String assembliesHome;

    @Override
    public boolean isOpened() {
        return isOpened;
    }

    @Override
    public boolean isPausing() {
        return pause.get();
    }

    @Override
    public RabbitMQProducerConfig config() {
        return config;
    }

    @Override
    public void addRoutingKey(String routingKey) throws CircuitException {
        if (config.getRoutingKeys().contains(routingKey)) {
            return;
        }
        pause.set(true);
        config.getRoutingKeys().add(routingKey);
        flushConfig();
        try {
            lock.lock();
            pauseController.signalAll();
        } catch (Exception e) {
            throw new CircuitException("500", e);
        } finally {
            pause.set(false);
            lock.unlock();
        }

    }

    @Override
    public void removeRoutingKey(String routingKey) throws CircuitException {
        if (!config.getRoutingKeys().contains(routingKey)) {
            return;
        }
        pause.set(true);
        if (config.getRoutingKeys().remove(routingKey)) {
            flushConfig();
        }
        try {
            lock.lock();
            pauseController.signalAll();
        } catch (Exception e) {
            throw new CircuitException("500", e);
        } finally {
            pause.set(false);
            lock.unlock();
        }

    }

    protected String selectRouteKey(String factor) throws CircuitException {
        List<String> keys = config.getRoutingKeys();
        if (keys.isEmpty()) {
            return null;
        }
        String sha1 = Encript.sha1(factor);
        int hash = Math.abs(sha1.hashCode());
        int index = hash % keys.size();
        return keys.get(index);
    }

    @Override
    public void innerOpen() throws CircuitException {
        if (!isOpened) {
            open(assembliesHome);
        }
    }

    @Override
    public Channel open(String assembliesHome) throws CircuitException {
        this.assembliesHome = assembliesHome;
        lock = new ReentrantLock();
        pauseController = lock.newCondition();
        assembliesHome = assembliesHome.endsWith("/") ? assembliesHome + "conf" + File.separator : assembliesHome + File.separator + "conf" + File.separator;
        System.out.println(assembliesHome);
        File homeDir = new File(assembliesHome);
        if (!homeDir.exists()) {
            homeDir.mkdirs();
        }
        confFile = new File(String.format("%sconfig.json", assembliesHome));
        if (!confFile.exists()) {
            throw new CircuitException("404", "配置文件不存在：" + confFile);
        }
        Reader reader = null;
        try {
            reader = new FileReader(confFile);
            config = new Gson().fromJson(reader, RabbitMQProducerConfig.class);
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
        String host = config.getHost();
        int port = config.getPort();
        String virtualHost = config.getVirtualHost();
        String user = config.getUser();
        String pwd = config.getPwd();
        String exchange = config.getExchange();
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try {
            connectionFactory.setHost(host);
            connectionFactory.setPort(port);
            connectionFactory.setVirtualHost(virtualHost);
            connectionFactory.setUsername(user);
            connectionFactory.setPassword(pwd);
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(exchange, "direct", true);
            CJSystem.logging().info(getClass(), "连接mq成功，配置如下:");
            config.printLog();
            isOpened = true;
            return channel;
        } catch (TimeoutException e) {
            throw new CircuitException("500", e);
        } catch (IOException e) {
            throw new CircuitException("500", e);
        }
    }

    @Override
    public void flushConfig() throws CircuitException {
        String json = new Gson().toJson(config);
        FileWriter writer = null;
        try {
            writer = new FileWriter(confFile);
            writer.write(json);
        } catch (IOException e) {
            throw new CircuitException("500", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
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
        isOpened = false;
    }


    @Override
    public void publish(AMQP.BasicProperties props, byte[] body) throws CircuitException {
        if (pause.get()) {
            CJSystem.logging().info(getClass(), "暂停发送消息，正在更新routingKeys列表...");
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
        /*
        1. mandatory标志位
        当mandatory标志位设置为true时，如果exchange根据自身类型和消息routeKey无法找到一个符合条件的queue，那么会调用basic.return方法将消息返还给生产者；当mandatory设为false时，出现上述情形broker会直接将消息扔掉。

        2. immediate标志位
        当immediate标志位设置为true时，如果exchange在将消息route到queue(s)时发现对应的queue上没有消费者，那么这条消息不会放入队列中。当与消息routeKey关联的所有queue(一个或多个)都没有消费者时，该消息会通过basic.return方法返还给生产者。
       */

        String routingKey = selectRouteKey(body.hashCode() + UUID.randomUUID().toString());
        try {
            channel.basicPublish(config.getExchange(), routingKey, true, false, props, body);
            channel.addReturnListener(this);
        } catch (IOException e) {
            throw new CircuitException("500", e);
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
