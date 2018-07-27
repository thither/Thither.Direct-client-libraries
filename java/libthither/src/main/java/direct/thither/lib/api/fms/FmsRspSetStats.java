package direct.thither.lib.api.fms;
/*
 * Author Kashirin Alex (kashirin.alex@gmail.com)
 * THITHER.DIRECT
 * */

import java.util.List;
import java.util.Arrays;

public class FmsRspSetStats extends FmsRsp {
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

