package cj.netos.rabbitmq;

import cj.studio.ecm.CJSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoutingNode {
    String title;
    Castmode castmode;
    List<String> routingKeys;

    public static RoutingNode parse(Map<String, Object> map) {
        RoutingNode node = new RoutingNode();
        node.load(map);
        return node;
    }

    private void load(Map<String, Object> map) {
        title = map.get("title") == null ? "" : (String) map.get("title");
        castmode = map.get("castmode") == null ? Castmode.unicast : Castmode.valueOf((String) map.get("castmode"));
        routingKeys = map.get("routingKeys") == null ? new ArrayList<>() : (List<String>) map.get("routingKeys");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Castmode getCastmode() {
        return castmode;
    }

    public void setCastmode(Castmode castmode) {
        this.castmode = castmode;
    }

    public List<String> getRoutingKeys() {
        return routingKeys;
    }

    public void setRoutingKeys(List<String> routingKeys) {
        this.routingKeys = routingKeys;
    }

    public void printLog(Class aClass, String indent) {
        CJSystem.logging().info(aClass, String.format("%stitle=%s", indent, title));
        CJSystem.logging().info(aClass, String.format("%scastmode=%s", indent, castmode));
        CJSystem.logging().info(aClass, String.format("%sroutingKeys:", indent));
        for (String key : routingKeys) {
            CJSystem.logging().info(aClass, String.format("%s\t- %s", indent, key));
        }
    }
}
