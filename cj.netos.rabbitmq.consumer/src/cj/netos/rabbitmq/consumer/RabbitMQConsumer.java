package cj.netos.rabbitmq.consumer;

import cj.netos.rabbitmq.IConsumer;
import cj.netos.rabbitmq.IRabbitMQConsumer;
import cj.netos.rabbitmq.RabbitMQConsumerConfig;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import com.rabbitmq.client.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@CjService(name = "rabbitMQConsumer")
public class RabbitMQConsumer implements IRabbitMQConsumer {
    private boolean isOpened;
    RabbitMQConsumerConfig config;
    Channel channel;
    Connection connection;
    File confFile;
    ReentrantLock lock;
    Condition pauseController;
    private String assembliesHome;


    @Override
    public boolean isOpened() {
        return isOpened;
    }


    @Override
    public RabbitMQConsumerConfig config() {
        return config;
    }

    @Override
    public Channel innerOpen() throws CircuitException {
        return open(assembliesHome);
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
            config = new Gson().fromJson(reader, RabbitMQConsumerConfig.class);
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
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try {
            connectionFactory.setHost(host);
            connectionFactory.setPort(port);
            connectionFactory.setVirtualHost(virtualHost);
            connectionFactory.setUsername(user);
            connectionFactory.setPassword(pwd);
            connectionFactory.setAutomaticRecoveryEnabled(false);

            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
            //设置客户端最多接收未被ack的消息的个数
            channel.basicQos(1);
            //4 声明交换机和队列，然后进行绑定设置路由Key
            channel.queueDeclare(config.getQueueName(), true, false, false, null);
            for (String exchange : config.getExchanges()) {
                channel.exchangeDeclare(exchange, "direct", true, false, null);
                channel.queueBind(config.getQueueName(), exchange, config.getRoutingKey());
            }
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
    public void acceptConsumer(IConsumer consumer) throws CircuitException {
        //5 设置channel，使用自定义消费者
        try {
            channel.basicConsume(config.getQueueName(), false, new CLAFConsumer(channel, consumer));
        } catch (IOException e) {
            throw new CircuitException("500", e);
        }
    }
}

class CLAFConsumer extends DefaultConsumer {
    IConsumer consumer;

    public CLAFConsumer(Channel channel, IConsumer consumer) {
        super(channel);
        this.consumer = consumer;
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        CJSystem.logging().error(getClass(), sig);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        consumer.handleDelivery(getChannel(), consumerTag, envelope, properties, body);
    }
}
