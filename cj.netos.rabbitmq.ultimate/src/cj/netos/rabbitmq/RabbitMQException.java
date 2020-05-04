package cj.netos.rabbitmq;


import java.io.IOException;
import java.util.Stack;

public class RabbitMQException extends IOException {
    protected String status;

    public RabbitMQException(String status) {
        super();
        this.status = status;
    }

    public RabbitMQException(String status, String message) {
        super(message);
        this.status = status;
    }

    public RabbitMQException(String status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public RabbitMQException(String status, Throwable cause) {
        super(cause);
        this.status = status;
    }

    public static RabbitMQException search(Throwable e) {
        Stack<Throwable> stack = new Stack<Throwable>();
        RabbitMQException result = null;
        Throwable tmp = e;
        do {// 正序搜系统异常
            if (tmp instanceof RabbitMQException) {
                RabbitMQException d = (RabbitMQException) tmp;
                result = d;
                break;
            }
            stack.push(tmp);
        } while ((tmp = tmp.getCause()) != null);
        // 如果不存在系统异常则反序找回路异常
        while (result == null && !stack.isEmpty()) {
            tmp = stack.pop();
            if (tmp instanceof RabbitMQException) {
                result = (RabbitMQException) tmp;
                break;

            }
        }
        return result;
    }

    public String getStatus() {
        return status;
    }
}
