package cj.netos.rabbitmq;

import cj.studio.ecm.CJSystem;

import java.util.ArrayList;
import java.util.List;

public class RabbitMQProducerConfig {
    String host;
    int port;
    String virtualHost;
    String user;
    String pwd;
    String exchange;
    List<String> routingKeys;

    public RabbitMQProducerConfig() {
        routingKeys =new ArrayList<>();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public List<String> getRoutingKeys() {
        return routingKeys;
    }

    public void setRoutingKeys(List<String> routingKeys) {
        this.routingKeys = routingKeys;
    }

    public void printLog() {
        CJSystem.logging().info(getClass(),String.format("host=%s",host));
        CJSystem.logging().info(getClass(),String.format("port=%s",port));
        CJSystem.logging().info(getClass(),String.format("virtualHost=%s",virtualHost));
        CJSystem.logging().info(getClass(),String.format("user=%s",user));
        CJSystem.logging().info(getClass(),String.format("pwd=%s",pwd));
        CJSystem.logging().info(getClass(),String.format("exchange=%s",exchange));
        CJSystem.logging().info(getClass(),String.format("routingKeys(%s个):", routingKeys.size()));
        for (String key : this.routingKeys) {
            CJSystem.logging().info(getClass(),String.format("\t%s",key));
        }
    }
}
