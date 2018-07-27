package direct.thither.lib.api.fms;
/*
 * Author Kashirin Alex (kashirin.alex@gmail.com)
 * THITHER.DIRECT
 * */

import org.json.JSONObject;
import java.util.concurrent.ConcurrentHashMap;

public class FmsRspGetDefinitions extends FmsRsp {

    ConcurrentHashMap<String, FmsDefinition> units;
    ConcurrentHashMap<String, FmsDefinition> sections;
    ConcurrentHashMap<String, FmsDefinition> metrics;

    public FmsRspGetDefinitions(int status_code, String message_code, String message){
        code = status_code;
        msg_code = message_code;
        msg = message;
    }
    public FmsRspGetDefinitions(int status_code, JSONObject js_rsp){
        code = status_code;

        JSONObject items;
        JSONObject item;
        if(js_rsp.has("units")){
            items = js_rsp.getJSONObject("units");
            if(units==null)
                units = new ConcurrentHashMap<>();
            for(String k :items.keySet()) {
                item = items.getJSONObject(k);
                units.put(k, new FmsDefinition(FmsDefinitionType.UNITS, k, item.getString("name")));
            }
        }
        if(js_rsp.has("sections")){
            items = js_rsp.getJSONObject("sections");
            if(sections==null)
                sections = new ConcurrentHashMap<>();
            for(String k :items.keySet()) {
                item = items.getJSONObject(k);
                sections.put(k, new FmsDefinition(FmsDefinitionType.SECTIONS, k, item.getString("name"),
                        item.optString("desc", null)));
            }
        }
        if(js_rsp.has("metrics")){
            items = js_rsp.getJSONObject("metrics");
            if(metrics==null)
                metrics = new ConcurrentHashMap<>();
            for(String k :items.keySet()) {
                item = items.getJSONObject(k);
                metrics.put(k, new FmsDefinition(FmsDefinitionType.METRICS, k, item.getString("name"),
                        item.optString("desc", null),
                        item.optString("section", null),
                        item.optString("unit", null),
                        item.optString("operation", null),
                        Integer.valueOf(item.optString("tz", null)),
                        Integer.valueOf(item.optString("timebase", null))
                ));
            }
        }

    }

    public String toString() {
        return getClass().getSimpleName()+ _toString() +String.format(
                "(%s=%s, %s=%s, %s=%s)",
                "\n\nunits", units, "\n\nsections", sections, "\n\nmetrics", metrics);
    }
}
