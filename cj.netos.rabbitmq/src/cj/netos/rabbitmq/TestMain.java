package cj.netos.rabbitmq;

import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class TestMain {
    public static void main(String[] args) {
        String host = "47.104.128.137";
        int port = 5675;
        String virtualHost = "/test";
        String user = "superadmin";
        String pwd = "123456";
        String exchange = "test_exchange";
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try {
            connectionFactory.setHost(host);
            connectionFactory.setPort(port);
            connectionFactory.setVirtualHost(virtualHost);
            connectionFactory.setUsername(user);
            connectionFactory.setPassword(pwd);
//            connectionFactory.setConnectionTimeout();
//            connectionFactory.setWorkPoolTimeout();
//            connectionFactory.setRequestedHeartbeat();
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            //经验证，一个channel上可以定义多个交换器，但如果一个交换器已被定义，则不可再通过exchangeDeclare修改，那怕是变更属性都不可
            try {
                channel.exchangeDeclarePassive(exchange);
            } catch (IOException e) {
                Throwable cause = e.getCause();
                if (cause instanceof ShutdownSignalException) {
                    ShutdownSignalException shutdownSignalException = (ShutdownSignalException) cause;
                    AMQP.Channel.Close method = (AMQP.Channel.Close) shutdownSignalException.getReason();
                    int replyCode = method.getReplyCode();
                    if (replyCode == 404) {
                        throw new CircuitException("404", String.format("RabbitMQ上不存在交换器 %s，请先启动消息生产者节点以生成Exchange，或者按生产者节点的交换机配置手工在rabbitmq上配置该交换器", exchange));
                    }
                }
                throw new CircuitException("500", e);
            }
//            channel.exchangeDeclare(exchange, "direct", true,false,null);
            channel.close();
            connection.close();
            CJSystem.logging().info("连接mq成功，配置如下:");
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException | CircuitException e) {
            e.printStackTrace();
        }
    }
}
