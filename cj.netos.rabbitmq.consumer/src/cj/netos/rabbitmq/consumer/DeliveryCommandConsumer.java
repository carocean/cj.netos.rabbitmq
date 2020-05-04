package cj.netos.rabbitmq.consumer;

import cj.netos.rabbitmq.IConsumer;
import cj.netos.rabbitmq.RabbitMQException;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.IServiceProvider;
import cj.studio.ecm.ServiceCollection;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.LongString;
import com.rabbitmq.client.impl.LongStringHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 分派命令的消费器<br>
 *     格式如：
 *     /test/myservice.ports#getName
 */
public class DeliveryCommandConsumer implements IConsumer {
    Map<String, IConsumerCommand> commandMap;

    public DeliveryCommandConsumer(IServiceProvider site) {
        commandMap = new HashMap<>();
        ServiceCollection<IConsumerCommand> commands = site.getServices(IConsumerCommand.class);
        for (IConsumerCommand cmd : commands) {
            CjService cjService = cmd.getClass().getAnnotation(CjService.class);
            if (cjService == null) {
                continue;
            }
            if (!cjService.name().startsWith("/") && cjService.name().lastIndexOf("#") < 0) {
                CJSystem.logging().warn(getClass(), String.format("消费指令定义错误，已忽略。路径必须以/开头且必须含有#号，错误格式为：%s", cjService.name()));
                continue;
            }
            commandMap.put(cjService.name(), cmd);
            CJSystem.logging().info(getClass(), String.format("发现消费指令：%s", cjService.name()));
        }
    }

    @Override
    public void handleDelivery(Channel channel, String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws RabbitMQException {
//        CJSystem.logging().info(getClass(), String.format("收到消息:\r\n%s\r\n%s", properties.getHeaders(), new String(body)));
        String uri = properties.getType();
        Map<String,Object> headers=properties.getHeaders();
        LongString commandData = (LongString) headers.get("command");
        String command=commandData.toString();
        String url = String.format("%s#%s", uri, command);
        IConsumerCommand consumerCommand = commandMap.get(url);
        if (consumerCommand == null) {
            throw new RabbitMQException("500", "不支持的命令：" + url);
        }
        try {
            consumerCommand.command(consumerTag, envelope, properties, body);
            channel.basicAck(envelope.getDeliveryTag(),false);
        } catch (Throwable throwable) {
            RabbitMQException ce=RabbitMQException.search(throwable);
            if (ce != null) {
                throw ce;
            }
            throw new RabbitMQException("500",throwable);
        }
    }
}
