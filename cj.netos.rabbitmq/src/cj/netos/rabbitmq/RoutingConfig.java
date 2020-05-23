package cj.netos.rabbitmq;

import cj.studio.ecm.CJSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutingConfig {

    boolean mandatory;
    boolean immediate;

    Map<String,RoutingNode> routingNodes;

    public void parse(Map<String, Object> map) {
        if (map == null) {
            map = new HashMap<>();
        }

        mandatory = map.get("mandatory") == null ? true : (boolean) map.get("mandatory");
        immediate = map.get("immediate") == null ? false : (boolean) map.get("immediate");

        Map<String, Map<String, Object>> routingNodesConf = (Map<String, Map<String, Object>>) map.get("routingNodes");
        for (Map.Entry<String, Map<String, Object>> entry : routingNodesConf.entrySet()) {
            RoutingNode node = RoutingNode.parse(entry.getValue());
            routingNodes.put(entry.getKey(), node);
        }

    }
    public void printLog(Class aClass, String indent) {
        CJSystem.logging().info(aClass, String.format("%smandatory=%s",  indent,mandatory));
        CJSystem.logging().info(aClass, String.format("%simmediate=%s",  indent,immediate));
        CJSystem.logging().info(aClass, String.format("%sroutingNodes:", indent));
        for (String key : routingNodes.keySet()) {
            RoutingNode node = routingNodes.get(key);
            CJSystem.logging().info(aClass, String.format("%s%s%s", indent, indent, key));
            node.printLog(aClass, indent + indent + indent);
        }
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isImmediate() {
        return immediate;
    }

    public void setImmediate(boolean immediate) {
        this.immediate = immediate;
    }

    public Map<String, RoutingNode> getRoutingNodes() {
        return routingNodes;
    }

    public void setRoutingNodes(Map<String, RoutingNode> routingNodes) {
        this.routingNodes = routingNodes;
    }
}
