package cj.netos.rabbitmq;

import cj.studio.ecm.CJSystem;

import java.util.HashMap;
import java.util.Map;

public class QueueConfig {
    String name;
    boolean durable;
    boolean autoDelete;
    boolean exclusive;
    Map<String, Object> arguments;

    public void parse(Map<String, Object> node) {
        name = node.get("name") == null ? "" : (String) node.get("name");
        durable = node.get("durable") == null ? false : (boolean) node.get("durable");
        autoDelete = node.get("autoDelete") == null ? true : (boolean) node.get("autoDelete");
        exclusive = node.get("exclusive") == null ? false : (boolean) node.get("exclusive");
        arguments = node.get("arguments") == null ? new HashMap<>() : (Map<String, Object>) node.get("arguments");
    }

    public void printLog(Class<? extends RabbitMQConsumerConfig> aClass, String indent) {
        CJSystem.logging().info(aClass, String.format("%sname=%s", indent, name));
        CJSystem.logging().info(aClass, String.format("%sdurable=%s", indent, durable));
        CJSystem.logging().info(aClass, String.format("%sautoDelete=%s", indent, autoDelete));
        CJSystem.logging().info(aClass, String.format("%sexclusive=%s", indent, exclusive));
        CJSystem.logging().info(aClass, String.format("%sarguments=%s", indent, arguments));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }


}
