package direct.thither.lib.api.fms;
/*
 * Author Kashirin Alex (kashirin.alex@gmail.com)
 * THITHER.DIRECT
 * */

public class FmsSetStatsItem extends FmsReq{
    public String dt,v,e;

    private String normalize_ts(long ts){
        String s_ts = ((Long)ts).toString();
        if (s_ts.length()>10) s_ts = s_ts.substring(0, 10);
        return s_ts;
    }

    public FmsSetStatsItem(String metric_id){
        mid = metric_id;
    }
    public void set_details(String date_time, String value){
        dt = date_time;
        v = value;
    }
    public void set_details(String date_time, Long value){
        dt = date_time;
        v = value.toString();
    }
    public void set_details(long date_time, String value){
        dt = normalize_ts(date_time);
        v = value;
    }
    public void set_details(long date_time, Long value){
        dt = normalize_ts(date_time);
        v = value.toString();
    }

    public FmsSetStatsItem(String metric_id, String date_time, String value){
        mid = metric_id;
        dt = date_time;
        v = value;
    }
    public FmsSetStatsItem(String metric_id, String date_time, Long value){
        mid = metric_id;
        dt = date_time;
        v = value.toString();
    }
    public FmsSetStatsItem(String metric_id, long date_time, String value){
        mid = metric_id;
        dt = normalize_ts(date_time);
        v = value;
    }
    public FmsSetStatsItem(String metric_id, long date_time, Long value){
        mid = metric_id;
        dt = normalize_ts(date_time);
        v = value.toString();
    }

    public FmsSetStatsItem(String error, String metric_id, String date_time, String value){
        mid = metric_id;
        dt = date_time;
        v = value;
        e = error;
    }

    public String to_csv_line(){
        if(mid == null || dt == null || v ==  null) return "";
        return mid+","+dt+","+v+"\n";
    }
    public String toString(){
        String s = getClass().getSimpleName()+_toString()+String.format("(%s=%s, %s=%s)" ,"date_time", dt, "value", v);
        if(e != null)
            return s + String.format("(%s=%s)" , "error", e);
        return s;
    }
}

