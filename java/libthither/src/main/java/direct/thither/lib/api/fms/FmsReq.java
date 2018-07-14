package direct.thither.lib.api.fms;
/*
 * Author Kashirin Alex (kashirin.alex@gmail.com)
 * THITHER.DIRECT
 * */

public class FmsReq {
    String mid;

    public String _toString(){
        return String.format("(%s=%s)" , "metric_id", mid);
    }
}


class FmsSetStatsItem extends FmsReq{
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
        return mid+","+dt+","+v+"\n";
    }
    public String toString(){
        String s = getClass().getSimpleName()+_toString()+String.format("(%s=%s, %s=%s)" ,"date_time", dt, "value", v);
        if(e != null)
            return s + String.format("(%s=%s)" , "error", e);
        return s;
    }
}


class FmsGetStatsQuery extends FmsReq{
    public String from, to, base, tz, tf, limit, page;

    public FmsGetStatsQuery(String metric_id, Long from_ts, Long to_ts){
        mid = metric_id;
        from = from_ts.toString();
        to = to_ts.toString();
    }
    public void set_grouping(Integer minutes_base, Integer minutes_timezone, String time_format){
        if(minutes_base != null)
            base = minutes_base.toString();
        if(minutes_timezone != null)
            tz = minutes_timezone.toString();
        tf = time_format;
    }
    public void set_limit(Integer l){
        limit = l.toString();
    }
    public void set_page(Integer p){
        page = p.toString();
    }
    public String toString(){
        return getClass().getSimpleName()+_toString()+String.format(
                "(%s=%s, %s=%s, %s=%s, %s=%s, %s=%s, %s=%s, %s=%s)" ,
                "from", from, "to", to, "base", base, "tz", tz, "tf", tf, "limit", limit, "page", page);
    }
}
