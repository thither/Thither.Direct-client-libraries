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

