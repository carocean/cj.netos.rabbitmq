package cj.netos.rabbitmq;


import cj.studio.ecm.net.CircuitException;

public class RabbitMQException extends CircuitException {
    public RabbitMQException(CircuitException e) {
        super(e.getStatus(), e.getMessage());
    }

    public RabbitMQException(String status, Throwable e) {
        super(status, e);
    }

    public RabbitMQException(String status, boolean isSystemException, Throwable e) {
        super(status, isSystemException, e);
    }

    public RabbitMQException(String status, String e) {
        super(status, e);
    }

    public RabbitMQException(String status, boolean isSystemException, String e) {
        super(status, isSystemException, e);
    }

}
