package cj.netos.rabbitmq;

import cj.netos.rabbitmq.consumer.DeliveryCommandConsumer;
import cj.netos.rabbitmq.consumer.RabbitMQConsumer;
import cj.netos.rabbitmq.producer.RabbitMQProducer;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.context.IServiceContainerMonitor;
import cj.studio.ecm.net.CircuitException;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultRabbitMQMonitor implements IServiceContainerMonitor {
    Map<String, IRabbitMQConsumer> consumerMap;

    @Override
    public void onBeforeRefresh(IServiceSite site) {
        String assembliesHome = site.getProperty("home.dir");
        assembliesHome = assembliesHome.endsWith("/") ? assembliesHome + "rabbitmq" + File.separator : assembliesHome + File.separator + "rabbitmq" + File.separator;
        File homeDir = new File(assembliesHome);
        if (!homeDir.exists()) {
            homeDir.mkdirs();
        }
        CJSystem.logging().info(getClass(), String.format("rabbitmq的配置根目录是:%s", homeDir));
        String producerHome = String.format("%sproducer%s", assembliesHome, File.separator);
        File homeProducer = new File(producerHome);
        if (!homeProducer.exists()) {
            homeProducer.mkdirs();
        }
        CJSystem.logging().info(getClass(), String.format("开始扫描生产者配置..."));
        try {
            scaneProducerHome(homeProducer, site);
        } catch (CircuitException e) {
            throw new EcmException(e);
        }
        CJSystem.logging().info(getClass(), String.format("完成扫描生产者配置"));

        String consumerHome = String.format("%sconsumer%s", assembliesHome, File.separator);
        File homeConumer = new File(consumerHome);
        if (!homeConumer.exists()) {
            homeConumer.mkdirs();
        }
        CJSystem.logging().info(getClass(), String.format("开始扫描消费者配置..."));
        consumerMap = new HashMap<>();
        try {
            scaneConsumerHome(homeConumer, site);
        } catch (RabbitMQException e) {
            throw new EcmException(e);
        }
        CJSystem.logging().info(getClass(), String.format("完成扫描消费者配置"));
    }


    protected void scaneProducerHome(File home, IServiceSite site) throws CircuitException {
        File[] files = home.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".json");
            }
        });
        for (File file : files) {
            String fn = file.getName();
            int pos = fn.lastIndexOf(".");
            String name = fn.substring(0, pos);
            CJSystem.logging().info(getClass(), String.format("\t发现生产者:%s", name));
            IRabbitMQProducer producer = new RabbitMQProducer();
            producer.open(file);
            String serviceName = String.format("@.rabbitmq.producer.%s", name);
            site.addService(serviceName, producer);
            CJSystem.logging().info(getClass(), String.format("\t成功打开生产者:%s，服务名为:%s。即：可以在程序中作为服务注入使用", name, serviceName));
        }
    }

    protected void scaneConsumerHome(File home, IServiceSite site) throws RabbitMQException {
        File[] files = home.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".json");
            }
        });
        for (File file : files) {
            String fn = file.getName();
            int pos = fn.lastIndexOf(".");
            String name = fn.substring(0, pos);
            CJSystem.logging().info(getClass(), String.format("\t发现消费者:%s", name));
            IRabbitMQConsumer consumer = new RabbitMQConsumer();
            consumer.open(file);
            site.addService(name, consumer);
            consumerMap.put(name, consumer);
            CJSystem.logging().info(getClass(), String.format("\t成功打开消费者:%s。使用说明：", name));
            CJSystem.logging().info(getClass(), String.format("\t\t\t* 定义消费指令类需要实现接口:IConsumerCommand，服务名声明格式为:/xx/xx.ports#command。并使用注解 @CJConsumer(name=\"%s\")", name));
            CJSystem.logging().info(getClass(), String.format("\t\t\t* 生产者发送的消息的Properties.type对应消费指令类的注解@CjService(name=)中name的地址，并在其headers中放入对应的command命令"));
        }
    }

    @Override
    public void onAfterRefresh(IServiceSite site) {
        for (Map.Entry<String, IRabbitMQConsumer> entry : consumerMap.entrySet()) {
            try {
                entry.getValue().acceptConsumer(new DeliveryCommandConsumer(site, entry.getKey()));
            } catch (RabbitMQException e) {
                throw new EcmException(e);
            }
        }
    }
}
