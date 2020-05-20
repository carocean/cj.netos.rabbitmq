package cj.netos.rabbitmq;


import cj.studio.ecm.net.CircuitException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import java.io.File;

public interface IRabbitMQProducer {
    boolean isOpened();

    boolean isPausing();

    RabbitMQProducerConfig config();


    Channel open(File confFile) throws CircuitException;

    void refreshConfig() throws CircuitException;

    void close() throws CircuitException;


    void publish(AMQP.BasicProperties props, byte[] body) throws CircuitException;

    void reopen() throws CircuitException;

}
