package cj.netos.rabbitmq;

import cj.studio.ecm.net.CircuitException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

public interface IConsumer {
    void handleDelivery(Channel channel, String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws RabbitMQException;

}
