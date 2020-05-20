package cj.netos.rabbitmq;

import cj.studio.ecm.CJSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutingConfig {
    DirectCast directCast;
    boolean mandatory;
    boolean immediate;
    List<String> routingKeys;

    public void parse(Map<String, Object> map) {
        if (map == null) {
            map = new HashMap<>();
        }
        directCast = map.get("directCast") == null ? DirectCast.unicast : DirectCast.valueOf((String) map.get("directCast"));
        mandatory = map.get("mandatory") == null ? true : (boolean) map.get("mandatory");
        immediate = map.get("immediate") == null ? false : (boolean) map.get("immediate");
        routingKeys = map.get("routingKeys") == null ? new ArrayList<>() : (List<String>) map.get("routingKeys");
    }
    public void printLog(Class aClass, String indent) {
        CJSystem.logging().info(aClass, String.format("%sdirectCast=%s", indent,directCast));
        CJSystem.logging().info(aClass, String.format("%smandatory=%s",  indent,mandatory));
        CJSystem.logging().info(aClass, String.format("%simmediate=%s",  indent,immediate));
        CJSystem.logging().info(aClass, String.format("%sroutingKeys:", indent));
        for (String key : routingKeys) {
            CJSystem.logging().info(aClass, String.format("%s%sdirectCast=%s", indent, indent, key));
        }
    }
    public DirectCast getDirectCast() {
        return directCast;
    }

    public void setDirectCast(DirectCast directCast) {
        this.directCast = directCast;
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

    public List<String> getRoutingKeys() {
        return routingKeys;
    }

    public void setRoutingKeys(List<String> routingKeys) {
        this.routingKeys = routingKeys;
    }



}
