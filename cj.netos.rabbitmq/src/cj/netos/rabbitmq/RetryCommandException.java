package cj.netos.rabbitmq;


import java.util.Stack;

public class RetryCommandException extends Exception {
    protected String status;

    public RetryCommandException(String status) {
        super();
        this.status = status;
    }

    public RetryCommandException(String status, String message) {
        super(message);
        this.status = status;
    }

    public RetryCommandException(String status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public RetryCommandException(String status, Throwable cause) {
        super(cause);
        this.status = status;
    }

    public static RetryCommandException search(Throwable e) {
        Stack<Throwable> stack = new Stack<Throwable>();
        RetryCommandException result = null;
        Throwable tmp = e;
        do {// 正序搜系统异常
            if (tmp instanceof RetryCommandException) {
                RetryCommandException d = (RetryCommandException) tmp;
                result = d;
                break;
            }
            stack.push(tmp);
        } while ((tmp = tmp.getCause()) != null);
        // 如果不存在系统异常则反序找回路异常
        while (result == null && !stack.isEmpty()) {
            tmp = stack.pop();
            if (tmp instanceof RetryCommandException) {
                result = (RetryCommandException) tmp;
                break;

            }
        }
        return result;
    }

    public String getStatus() {
        return status;
    }
}
