package cj.netos.rabbitmq;

import cj.studio.ecm.CJSystem;

import java.util.Map;

public class BasicQosConfig {
    int prefetchSize;
    int prefetchCount;
    boolean global;

    public void parse(Map<String, Object> node) {
        prefetchSize = node.get("prefetchSize") == null ? 0 : (int) node.get("prefetchSize");
        prefetchCount = node.get("prefetchCount") == null ? 0 : (int) node.get("prefetchCount");
        global = node.get("global") == null ? false : (boolean) node.get("global");
    }

    public void printLog(Class<? extends RabbitMQConsumerConfig> aClass, String indent) {
        CJSystem.logging().info(aClass, String.format("%sprefetchSize=%s", indent, prefetchSize));
        CJSystem.logging().info(aClass, String.format("%sprefetchCount=%s", indent, prefetchCount));
        CJSystem.logging().info(aClass, String.format("%sglobal=%s", indent, global));
    }

    public int getPrefetchSize() {
        return prefetchSize;
    }

    public void setPrefetchSize(int prefetchSize) {
        this.prefetchSize = prefetchSize;
    }

    public int getPrefetchCount() {
        return prefetchCount;
    }

    public void setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }


}
