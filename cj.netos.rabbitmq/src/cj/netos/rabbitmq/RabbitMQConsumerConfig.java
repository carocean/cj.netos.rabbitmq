package cj.netos.rabbitmq;

import cj.studio.ecm.CJSystem;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RabbitMQConsumerConfig {
    String host;
    int port;
    String virtualHost;
    String user;
    String pwd;
    boolean automaticRecoveryEnabled;
    boolean autoAck;
    int requestedHeartbeat;
    int connectionTimeout;
    int workPoolTimeout;
    String routingKey;
    QueueConfig queue;
    List<String> exchanges;
    BasicQosConfig basicQos;
    Map<String, ExchangeConfig> autoCreateExchanges;

    public static RabbitMQConsumerConfig load(Reader reader) {
        RabbitMQConsumerConfig config = new RabbitMQConsumerConfig();
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
        autoAck = node.get("autoAck") == null ? true : (boolean) node.get("autoAck");
        requestedHeartbeat = node.get("requestedHeartbeat") == null ? 0 : (int) node.get("requestedHeartbeat");
        connectionTimeout = node.get("connectionTimeout") == null ? 0 : (int) node.get("connectionTimeout");
        workPoolTimeout = node.get("workPoolTimeout") == null ? 0 : (int) node.get("workPoolTimeout");
        routingKey = node.get("routingKey") == null ? "" : (String) node.get("routingKey");
        exchanges = node.get("exchanges") == null ? new ArrayList<>() : (List<String>) node.get("exchanges");
        queue = new QueueConfig();
        queue.parse((Map<String, Object>) node.get("queue"));
        basicQos = new BasicQosConfig();
        basicQos.parse((Map<String, Object>) node.get("basicQos"));
        parseAutoCreateExchanges((List<Map<String, Object>>) node.get("autoCreateExchanges"));
    }

    private void parseAutoCreateExchanges(List<Map<String, Object>> autoCreateExchanges) {
        this.autoCreateExchanges = new HashMap<>();
        for (Map<String, Object> item : autoCreateExchanges) {
            ExchangeConfig exchangeConfig = new ExchangeConfig();
            exchangeConfig.parse(item);
            this.autoCreateExchanges.put(exchangeConfig.getName(), exchangeConfig);
        }
    }

    public boolean isAutoAck() {
        return autoAck;
    }

    public void setAutoAck(boolean autoAck) {
        this.autoAck = autoAck;
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

    public QueueConfig getQueue() {
        return queue;
    }

    public void setQueue(QueueConfig queue) {
        this.queue = queue;
    }

    public BasicQosConfig getBasicQos() {
        return basicQos;
    }

    public void setBasicQos(BasicQosConfig basicQos) {
        this.basicQos = basicQos;
    }

    public Map<String, ExchangeConfig> getAutoCreateExchanges() {
        return autoCreateExchanges;
    }

    public void setAutoCreateExchanges(Map<String, ExchangeConfig> autoCreateExchanges) {
        this.autoCreateExchanges = autoCreateExchanges;
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
        CJSystem.logging().info(getClass(), String.format("queue:"));
        queue.printLog(getClass(), "\t");
        CJSystem.logging().info(getClass(), String.format("routingKey=%s", routingKey));
        CJSystem.logging().info(getClass(), String.format("autoAck=%s", autoAck));
        CJSystem.logging().info(getClass(), String.format("basicQos:"));
        basicQos.printLog(getClass(), "\t");
        CJSystem.logging().info(getClass(), String.format("exchanges(%s个):", exchanges.size()));
        for (String ex : this.exchanges) {
            CJSystem.logging().info(getClass(), String.format("\t%s", ex));
        }
        CJSystem.logging().info(getClass(), String.format("autoCreateExchanges:"));
        for (Map.Entry<String, ExchangeConfig> entry : autoCreateExchanges.entrySet()) {
            CJSystem.logging().info(getClass(), String.format("\t%s", entry.getKey()));
            entry.getValue().printLog(getClass(), "\t\t");
        }
    }
}
