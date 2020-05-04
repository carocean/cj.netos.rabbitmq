package cj.netos.rabbitmq;


import cj.studio.ecm.net.CircuitException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

public interface IRabbitMQProducer {
    boolean isOpened();

    boolean isPausing();

    RabbitMQProducerConfig config();


    Channel open(String appHome) throws CircuitException;

    void flushConfig() throws CircuitException;

    void close() throws CircuitException;

    void addRoutingKey(String routingKey) throws CircuitException;

    void removeRoutingKey(String routingKey) throws CircuitException;

    void publish(AMQP.BasicProperties props, byte[] body) throws CircuitException;

    void innerOpen() throws CircuitException;

}
