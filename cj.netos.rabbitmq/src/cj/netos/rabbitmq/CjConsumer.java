package cj.netos.rabbitmq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义消费指令类需要实现接口:IConsumerCommand，服务名声明格式为:/xx/xx.ports#command<br>
 *     生产者发送的消息的Properties.type对应消费指令类的注解@CjService(name=)中name的地址，并在其headers中放入对应的command命令
 */
@Target(value={ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CjConsumer {
    /**
     * 消费者名。
     * @return
     */
    public String name() ;
}
