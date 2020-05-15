package cj.netos.rabbitmq;


import cj.studio.ecm.net.CircuitException;
import com.rabbitmq.client.Channel;

import java.io.File;
import java.io.IOException;

public interface IRabbitMQConsumer {
    boolean isOpened();


    RabbitMQConsumerConfig config();


    Channel innerOpen() throws RabbitMQException;

    Channel open(File confFile) throws RabbitMQException;


    void close() throws RabbitMQException;


    void acceptConsumer(IConsumer consumer) throws  RabbitMQException;
}
