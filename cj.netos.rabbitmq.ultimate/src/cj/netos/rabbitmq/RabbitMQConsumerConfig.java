package cj.netos.rabbitmq;

import cj.studio.ecm.CJSystem;

import java.util.ArrayList;
import java.util.List;

public class RabbitMQConsumerConfig {
    String host;
    int port;
    String virtualHost;
    String user;
    String pwd;
    List<String> exchanges;
    String queueName;
    String routingKey;


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

    public List<String> getExchanges() {
        return exchanges;
    }

    public void setExchanges(List<String> exchanges) {
        this.exchanges = exchanges;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void printLog() {
        CJSystem.logging().info(getClass(),String.format("host=%s",host));
        CJSystem.logging().info(getClass(),String.format("port=%s",port));
        CJSystem.logging().info(getClass(),String.format("virtualHost=%s",virtualHost));
        CJSystem.logging().info(getClass(),String.format("user=%s",user));
        CJSystem.logging().info(getClass(),String.format("pwd=%s",pwd));
        CJSystem.logging().info(getClass(),String.format("queueName=%s",queueName));
        CJSystem.logging().info(getClass(),String.format("routingKey=%s", routingKey));
        CJSystem.logging().info(getClass(),String.format("exchanges(%s个):", exchanges.size()));
        for (String ex : this.exchanges) {
            CJSystem.logging().info(getClass(),String.format("\t%s",ex));
        }
    }
}
