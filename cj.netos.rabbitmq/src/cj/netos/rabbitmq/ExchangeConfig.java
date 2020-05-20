package cj.netos.rabbitmq;

import cj.studio.ecm.CJSystem;

import java.util.HashMap;
import java.util.Map;

public class ExchangeConfig {
    String name;
    String type;
    boolean durable;
    boolean autoDelete;
    boolean internal;
    Map<String, Object> arguments;

    public void parse(Map<String, Object> map) {
        if (map == null) {
            map = new HashMap<>();
        }
        name = (String) map.get("name");
        type = (String) map.get("type");
        durable = map.get("durable") == null ? false : (boolean) map.get("durable");
        autoDelete = map.get("autoDelete") == null ? false : (boolean) map.get("autoDelete");
        internal = map.get("internal") == null ? false : (boolean) map.get("internal");
        arguments = map.get("arguments") == null ? new HashMap<>() : (Map<String, Object>) map.get("arguments");
    }

    public void printLog(Class aClass, String indent) {
        CJSystem.logging().info(aClass, String.format("%sname=%s", indent, name));
        CJSystem.logging().info(aClass, String.format("%stype=%s", indent, type));
        CJSystem.logging().info(aClass, String.format("%sdurable=%s", indent, durable));
        CJSystem.logging().info(aClass, String.format("%sautoDelete=%s", indent, autoDelete));
        CJSystem.logging().info(aClass, String.format("%sinternal=%s", indent, internal));
        CJSystem.logging().info(aClass, String.format("%sarguments=%s", indent, arguments));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }


}
