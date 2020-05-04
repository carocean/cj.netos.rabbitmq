package cj.netos.rabbitmq.consumer;

import cj.netos.rabbitmq.RabbitMQException;
import cj.netos.rabbitmq.RetryCommandException;
import cj.studio.ecm.net.CircuitException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;

public interface IConsumerCommand {
    /**
     * 命令，如果未抛出异常则消息确认，否则消息不会被确认
     * @param consumerTag
     * @param envelope
     * @param properties
     * @param body
     * @throws RabbitMQException 通用异常，该异常会拒绝命令，并从rabbitmq队列中移除
     * @throws RetryCommandException 该异常会拒绝命令，并将命令重新放入rabbitmq队列
     */
    void command(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws RabbitMQException, RetryCommandException;
}
