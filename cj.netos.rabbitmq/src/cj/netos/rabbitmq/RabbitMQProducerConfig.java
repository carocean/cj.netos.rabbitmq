package cj.netos.rabbitmq;

import cj.studio.ecm.CJSystem;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RabbitMQProducerConfig {
    String host;
    int port;
    String virtualHost;
    String user;
    String pwd;
    boolean automaticRecoveryEnabled;
    int requestedHeartbeat;
    int connectionTimeout;
    int workPoolTimeout;
    ExchangeConfig exchange;
    RoutingConfig routing;


    public static RabbitMQProducerConfig load(Reader reader) {
        RabbitMQProducerConfig config = new RabbitMQProducerConfig();
        Yaml nodeyaml = new Yaml();
        Map<String, Object> node = nodeyaml.load(reader);
        config.parse(node);
        return config;
    }

    private void parse(Map<String, Object> node) {
        host = node.get("host") == null ? "" : node.get("host") + "";
        port = node.get("port") == null ? 80 : (int) node.get("port");
        virtualHost = node.get("virtualHost") == null ? "" : node.get("virtualHost") + "";
        user = node.get("user") == null ? "" : node.get("user") + "";
        pwd = node.get("pwd") == null ? "" : node.get("pwd") + "";
        automaticRecoveryEnabled = node.get("automaticRecoveryEnabled") == null ? false : (boolean) node.get("automaticRecoveryEnabled");
        requestedHeartbeat = node.get("requestedHeartbeat") == null ? 0 : (int) node.get("requestedHeartbeat");
        connectionTimeout = node.get("connectionTimeout") == null ? 0 : (int) node.get("connectionTimeout");
        workPoolTimeout = node.get("workPoolTimeout") == null ? 0 : (int) node.get("workPoolTimeout");
        Map<String, Object> exchangeMap = (Map<String, Object>) node.get("exchange");
        exchange = new ExchangeConfig();
        exchange.parse(exchangeMap);
        routing = new RoutingConfig();
        routing.parse((Map<String, Object>) node.get("routing"));
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

    public boolean isAutomaticRecoveryEnabled() {
        return automaticRecoveryEnabled;
    }

    public void setAutomaticRecoveryEnabled(boolean automaticRecoveryEnabled) {
        this.automaticRecoveryEnabled = automaticRecoveryEnabled;
    }

    public int getRequestedHeartbeat() {
        return requestedHeartbeat;
    }

    public void setRequestedHeartbeat(int requestedHeartbeat) {
        this.requestedHeartbeat = requestedHeartbeat;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getWorkPoolTimeout() {
        return workPoolTimeout;
    }

    public void setWorkPoolTimeout(int workPoolTimeout) {
        this.workPoolTimeout = workPoolTimeout;
    }

    public ExchangeConfig getExchange() {
        return exchange;
    }

    public void setExchange(ExchangeConfig exchange) {
        this.exchange = exchange;
    }

    public RoutingConfig getRouting() {
        return routing;
    }

    public void setRouting(RoutingConfig routing) {
        this.routing = routing;
    }

    public void printLog() {
        CJSystem.logging().info(getClass(), String.format("host=%s", host));
        CJSystem.logging().info(getClass(), String.format("port=%s", port));
        CJSystem.logging().info(getClass(), String.format("virtualHost=%s", virtualHost));
        CJSystem.logging().info(getClass(), String.format("user=%s", user));
        CJSystem.logging().info(getClass(), String.format("pwd=%s", pwd));
        CJSystem.logging().info(getClass(), String.format("automaticRecoveryEnabled=%s", this.automaticRecoveryEnabled));
        CJSystem.logging().info(getClass(), String.format("connectionTimeout=%s", this.connectionTimeout));
        CJSystem.logging().info(getClass(), String.format("requestedHeartbeat=%s", this.requestedHeartbeat));
        CJSystem.logging().info(getClass(), String.format("workPoolTimeout=%s", this.workPoolTimeout));
        CJSystem.logging().info(getClass(), String.format("exchange:"));
        exchange.printLog(getClass(), "\t");
        CJSystem.logging().info(getClass(), String.format("routing:"));
        routing.printLog(getClass(), "\t");
    }
}
