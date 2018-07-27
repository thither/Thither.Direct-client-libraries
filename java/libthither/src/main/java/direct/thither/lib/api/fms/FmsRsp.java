package direct.thither.lib.api.fms;
/*
 * Author Kashirin Alex (kashirin.alex@gmail.com)
 * THITHER.DIRECT
 * */


public class FmsRsp {
    public String msg_code, msg;
    public int code;

    public String _toString(){
        return String.format("(%s=%d, %s=%s, %s=%s)" , "code", code, "msg_code", msg_code, "msg", msg);
    }
}





