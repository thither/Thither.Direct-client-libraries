package direct.thither.lib.api.fms;
/*
 * Author Kashirin Alex (kashirin.alex@gmail.com)
 * THITHER.DIRECT
 * */

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class FmsRsp {
    public String msg_code, msg;
    public int code;

    public String _toString(){
        return String.format("(%s=%d, %s=%s, %s=%s)" , "code", code, "msg_code", msg_code, "msg", msg);
    }
}

class FmsRspSetStats extends FmsRsp {
    public int succeed, failed;
    public List<FmsSetStatsItem> errors;
    public FmsRspSetStats(int status_code, String message_code, String message){
        code = status_code;
        msg_code = message_code;
        msg = message;
    }
    public FmsRspSetStats(int status_code, String message_code, int s){
        code = status_code;
        msg_code = message_code;
        succeed = s;
    }
    public FmsRspSetStats(int status_code, String message_code, int s, int f, List<FmsSetStatsItem> e){
        code = status_code;
        msg_code = message_code;
        succeed = s;
        failed = f;
        errors = e;
    }
    public FmsRspSetStats(int status_code, String message_code, int f, List<FmsSetStatsItem> e){
        code = status_code;
        msg_code = message_code;
        failed = f;
        errors = e;
    }

    public String toString(){
        String s = getClass().getSimpleName()+_toString();
        if(succeed > 0)
            return s + String.format("(%s=%d)" , "succeed", succeed);
        if(failed > 0)
            return s + String.format("(%s=%d, %s=%s)" , "failed", failed, "errors", Arrays.toString(errors.toArray()));
        return s;
    }
}


class FmsRspGetStats  extends FmsRsp {
    public Integer next_page;
    public JSONArray items;
    private int nxt;

    public FmsRspGetStats(int status_code, Integer nxt_p, JSONArray js_items){
        code = status_code;
        next_page = nxt_p;
        items = js_items;
        nxt=0;
    }
    public FmsRspGetStats(int status_code, String message_code, String message){
        code = status_code;
        msg_code = message_code;
        msg = message;
    }
    public Map.Entry<String, Integer> next(){
        JSONArray item = items.getJSONArray(nxt);
        nxt+=1;
        return Map.entry(item.getString(0), item.getInt(1));
    }
    public boolean has_next(){
        return nxt < items.length();
    }
    public String toString() {
        return getClass().getSimpleName()+_toString() + String.format("(%s=%d, %s=%d, %s=%d)",
                "next_page", next_page, "length", items.length(), "nxt_item", nxt);
    }
}


enum FmsDefinitionType{ALL, UNITS, SECTIONS, METRICS}


class FmsDefinition{
    public String id, name, desc, operation, section_id, unit_id;
    public Integer timezone, timebase;
    public FmsDefinitionType type;

    public FmsDefinition(FmsDefinitionType typ, String identifier, String n){
        type = typ;
        id = identifier;
        name = n;
    }

    public FmsDefinition(FmsDefinitionType typ, String identifier, String n, String d){
        type = typ;
        id = identifier;
        name = n;
        desc = d;
    }

    // metric
    public FmsDefinition(FmsDefinitionType typ, String identifier, String n, String d,
                         String s, String u, String oper,
                         Integer tz, Integer base){
        type = typ;
        id = identifier;
        name = n;
        desc = d;
        section_id = s;
        unit_id = u;
        operation = oper;
        timezone = tz;
        timebase = base;
    }

    public String toString(){
        String s = getClass().getSimpleName();
        switch (type){
            case UNITS:
                s+=String.format(
                        "(%s=%s, %s=%s, %s=%s, %s=%s)",
                        "Type", type.name(), "id", id, "name", name, "desc", desc);
                break;
            case SECTIONS:
                s+=String.format(
                        "(%s=%s, %s=%s, %s=%s, %s=%s)",
                        "Type", type.name(), "id", id, "name", name, "desc", desc);
                break;
            case METRICS:
                s+=String.format(
                        "(%s=%s, %s=%s, %s=%s, %s=%s, %s=%s, %s=%s, %s=%s, %s=%d, %s=%d)",
                        "Type", type.name(), "id", id, "name", name, "desc", desc,
                        "section", section_id, "unit_id", unit_id, "operation", operation,
                        "timezone", timezone, "timebase", timebase);
                break;
        }
        return s;
    }
}
class FmsRspGetDefinitions extends FmsRsp {

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
