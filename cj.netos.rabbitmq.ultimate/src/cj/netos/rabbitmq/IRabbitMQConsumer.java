package cj.netos.rabbitmq;


import cj.studio.ecm.net.CircuitException;
import com.rabbitmq.client.Channel;

public interface IRabbitMQConsumer {
    boolean isOpened();


    RabbitMQConsumerConfig config();


    Channel innerOpen() throws CircuitException;

    Channel open(String appHome) throws CircuitException;


    void close() throws CircuitException;


    void acceptConsumer(IConsumer consumer) throws CircuitException;
}
