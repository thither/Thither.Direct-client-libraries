package direct.thither.lib.api.fms;
/*
 * Author Kashirin Alex (kashirin.alex@gmail.com)
 * THITHER.DIRECT
 * */

import org.json.JSONArray;
import java.util.List;
import java.util.Map;

public class FmsRspGetStats  extends FmsRsp {
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
    public Map.Entry<String, Long> next(){
        JSONArray item = items.getJSONArray(nxt);
        nxt+=1;
        return Map.entry(item.getString(0), item.getLong(1));
    }
    public boolean has_next(){
        return nxt < items.length();
    }
    public String toString() {
        return getClass().getSimpleName()+_toString() + String.format("(%s=%d, %s=%d, %s=%d)",
                "next_page", next_page, "length", items.length(), "nxt_item", nxt);
    }
}

