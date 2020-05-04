package cj.netos.rabbitmq.consumer;

import cj.netos.rabbitmq.RabbitMQException;
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
     * @throws RabbitMQException
     */
    void command(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws RabbitMQException;
}
