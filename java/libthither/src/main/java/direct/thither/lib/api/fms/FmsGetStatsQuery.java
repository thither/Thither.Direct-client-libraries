package direct.thither.lib.api.fms;
/*
 * Author Kashirin Alex (kashirin.alex@gmail.com)
 * THITHER.DIRECT
 * */


public class FmsGetStatsQuery extends FmsReq{
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
