package cj.netos.rabbitmq.consumer;

import cj.netos.rabbitmq.RabbitMQException;
import cj.netos.rabbitmq.RetryCommandException;
import cj.studio.ecm.net.CircuitException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

public interface IConsumerCommand {
    /**
     * 命令，如果未抛出异常则消息被确认并消费掉
     * @param consumerTag
     * @param envelope
     * @param properties
     * @param body
     * @throws RabbitMQException 通用异常，该异常会消费掉消息（即丢掉消息），不再重试
     * @throws RetryCommandException 该异常会将消息重新放入rabbitmq队列重试，如果autoAck=false才会如此
     * @throws IOException 该异常一般是rabbitmq的网络异常，会将消息重新放入rabbitmq队列重试，如果autoAck=false才会如此
     * @throws Throwable 非RabbitMQException，RetryCommandException，IOException异常，视为默认为异常Throwable。默认异常则会消费掉消息（即丢掉消息），不再重试
     */
    void command(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws RabbitMQException, RetryCommandException, IOException;
}
