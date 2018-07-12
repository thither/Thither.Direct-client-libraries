package direct.thither.lib.api.fms;
/*
 * Author Kashirin Alex (kashirin.alex@gmail.com)
 * THITHER.DIRECT
 * */


import java.io.*;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.zip.*;

import okhttp3.*;

import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.EAXBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.json.JSONArray;
import org.json.JSONObject;


public class FmsClient {
    private static String root_url = "://thither.direct/api/fms/";
    private OkHttpClient _http_client = null;

    private String fm_id;
    private String ps;
    private boolean https = true;
    private String u_push, u_get;
    private boolean ka = false;

    enum Ciphers {None, AES, }
    private Ciphers cipher = Ciphers.None;
    private KeyParameter cipher_key;
    private int cipher_nonce_len;

    public FmsClient(String flow_id) {
        fm_id = flow_id;
        set_https(true);
    }
    public void set_pass_phrase(String phrase){
        ps = phrase;
    }
    public void set_https(boolean secure){
        https = secure;
        String u = "http"+(secure?"s":"")+root_url;
        u_push = u+"post/";
        u_get = u+"get/";
    }
    public void set_cipher(Ciphers c){
        cipher = c;
        if(cipher == Ciphers.None)return;
        cipher_key=new KeyParameter(ps.getBytes());
        cipher_nonce_len = ps.length();
    }
    public void set_keep_alive(boolean keep_alive){
        ka = keep_alive;
    }
    private void set_http_client(){
        if(_http_client != null) return;
        List<Protocol> protocols = new ArrayList<>();
        if(https)
            protocols.add(Protocol.HTTP_2);
        protocols.add(Protocol.HTTP_1_1);
        _http_client = new OkHttpClient.Builder().protocols(protocols).build();
    }
    private String get_aes_token() throws Exception {
        EAXBlockCipher c = new EAXBlockCipher(new AESEngine());

        byte[] nonce = new byte[cipher_nonce_len];
        new SecureRandom().nextBytes(nonce);
        c.init(true, new AEADParameters(cipher_key, c.getBlockSize()*8, nonce,  new byte[0]));

        byte[] d = (Instant.now().getEpochSecond()+"|"+ps).getBytes();
        byte[] crp = new byte[c.getOutputSize(d.length)];
        c.doFinal(crp, c.processBytes(d, 0, d.length, crp,0));

        System.out.println("d.length:    "+d.length);
        System.out.println("d:           "+(new String(d)));
        System.out.println("OutputSize:  "+c.getOutputSize(d.length));
        System.out.println("crp.length:  "+crp.length);
        System.out.println("crp:         "+(new String(crp)));

        System.out.println("MAC len:     "+c.getMac().length);
        System.out.println("MAC:         "+(new String(c.getMac())));
        System.out.println("MAC b64:     "+(new String(Base64.getEncoder().encode(c.getMac()))));

        System.out.println("nonce len:   "+nonce.length);
        System.out.println("nonce:       "+(new String(nonce)));
        System.out.println("nonce b64:   "+new String(Base64.getEncoder().encode(nonce)));

        c.init(false, new AEADParameters(cipher_key, c.getBlockSize()*8, nonce, new byte[0]));
        byte[] datOut = new byte[c.getOutputSize(crp.length)];
        int resultLen = c.processBytes(crp, 0, crp.length, datOut, 0);
        c.doFinal(datOut, resultLen);

        System.out.println("datIn:        "+(new String(d)));
        System.out.println("datOut:       "+(new String(datOut)));

        return (new String(Base64.getEncoder().encode(nonce)))+"|"+
                (new String(Base64.getEncoder().encode(c.getMac())))+"|"+
                (new String(Base64.getEncoder().encode(crp)));
    }

    public FmsRspSetStats push_single(FmsSetStatsItem item){
        FormBody.Builder b = new FormBody.Builder()
                .add("fid", fm_id)
                .add("mid", item.mid)
                .add("dt", item.dt)
                .add("v", item.v);
        try {
            switch (cipher){
                case AES:
                    b.add("token", get_aes_token());
                    break;
                default:
                    b.add("ps", ps);
                    break;
            }
            return post(b.build());
        }catch (Exception e){
            return new FmsRspSetStats(1, "bad_request", e.getMessage());
        }
    }
    public FmsRspSetStats push_list(List<FmsSetStatsItem> items) {
        if(items.size()==0)
            return new FmsRspSetStats(0, "bad_request", "EMPTY_LIST");

        StringBuilder csv_data = new StringBuilder("mid,dt,v\n");
        for (int i=0; i<items.size(); i++) csv_data.append(items.get(i).to_csv_line());
        return push_csv_data(csv_data.toString());
    }
    public FmsRspSetStats push_csv_data(String csv_data) {
        String[] lines = csv_data.split("\\r?\\n", 2);
        if(lines.length<2)
            return new FmsRspSetStats(0, "bad_request", "EMPTY_CSV");

        MultipartBody.Builder b =  new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("csv", csv_data)
                .addFormDataPart("fid", fm_id);
        try {
            //b.addFormDataPart("csv", compress(csv_data.getBytes()));
            //b.addFormDataPart("comp", "zlib");
            switch (cipher){
                case AES:
                    b.addFormDataPart("token", get_aes_token());
                default:
                    b.addFormDataPart("ps", ps);
                    break;
            }
            return post(b.build());

        }catch (Exception e){
            return new FmsRspSetStats(1, "bad_request", e.getMessage());
        }
    }
    private FmsRspSetStats post(RequestBody req_body){

        Request.Builder req_b = new Request.Builder().url(u_push);
        if(ka) req_b.addHeader("connection", "keep-alive");
        req_b.addHeader("accept-encoding", "deflate");
        Request req = req_b.post(req_body).build();

        set_http_client();
        int code;
        String msg;
        try {
            Response rsp =_http_client.newCall(req).execute();
            JSONObject jsrsp;
            code = rsp.code();
            switch(code) {
                case 200:
                    jsrsp = new JSONObject(decompress(rsp.body().bytes()));
                    String msg_code = jsrsp.getString("status");
                    if(msg_code.equals("OK"))
                        return new FmsRspSetStats(code, msg_code, jsrsp.getInt("succeed"));

                    List<FmsSetStatsItem> errs = new ArrayList<>();
                    JSONArray ji;
                    JSONArray errs_js = jsrsp.getJSONArray("errors");
                    for(int i=0 ; i< errs_js.length(); i++){
                        ji = errs_js.getJSONArray(i);
                        errs.add(new FmsSetStatsItem(
                                ji.getString(0),
                                ji.getString(1),
                                ji.getString(2),
                                ji.getString(3)
                        ));
                    }
                    if(msg_code.equals("SOME_ERRORS"))
                        return new FmsRspSetStats(code, msg_code, jsrsp.getInt("succeed"), jsrsp.getInt("failed"), errs);
                    return new FmsRspSetStats(code, msg_code, jsrsp.getInt("failed"), errs);
                case 404:
                    msg = "BAD REQUEST URL";
                    break;
                case 503:
                    msg = "INTERNAL SERVER ERROR, CONTACT SUPPORT";
                    break;
                default:
                    jsrsp = new JSONObject(decompress(rsp.body().bytes()));
                    return new FmsRspSetStats(code, jsrsp.getString("status"), jsrsp.getString("msg"));
            }
        } catch (Exception e) {
            msg = e.getMessage();
            code = 0;
        }
        return new FmsRspSetStats(code, "bad_request", msg);
    }

    public FmsRspGetDefinitions get_definitions(FmsDefinitionType typ, HashMap<String,String> query){

        FormBody.Builder b = new FormBody.Builder().add("fid", fm_id);
        if(query!=null)
            for (String k: query.keySet()) b.add(k, query.get(k));

        try {
            switch (cipher){
                case AES:
                    b.add("token", get_aes_token());
                    break;
                default:
                    b.add("ps", ps);
                    break;
            }
        }catch (Exception e){
            return new FmsRspGetDefinitions(1, "bad_request", e.getMessage());
        }

        Request.Builder req_b = new Request.Builder().url(u_get+"definitions/"+typ.name().toLowerCase()+"s/");
        if(ka) req_b.addHeader("connection", "keep-alive");
        req_b.addHeader("accept-encoding", "deflate");

        set_http_client();
        int code;
        String msg;
        try {
            Response rsp =_http_client.newCall(req_b.post(b.build()).build()).execute();
            JSONObject jsrsp;
            code = rsp.code();
            switch(code) {
                case 200:
                    return new FmsRspGetDefinitions(code, new JSONObject(decompress(rsp.body().bytes())));
                case 404:
                    msg = "BAD REQUEST URL";
                    break;
                case 503:
                    msg = "INTERNAL SERVER ERROR, CONTACT SUPPORT";
                    break;
                default:
                    jsrsp = new JSONObject(decompress(rsp.body().bytes()));
                    return new FmsRspGetDefinitions(code, jsrsp.getString("status"), jsrsp.getString("msg"));
            }
        } catch (Exception e) {
            msg = e.getMessage();
            code = 0;
        }
        return new FmsRspGetDefinitions(code, "bad_request", msg);
    }

    public FmsRspGetStats get_stats(FmsGetStatsQuery query){
        if(query == null)
            return new FmsRspGetStats(1, "bad_request", "empty_FmsGetStatsQuery");

        FormBody.Builder b = new FormBody.Builder().add("fid", fm_id);
        if(query.mid == null)
            return new FmsRspGetStats(1, "bad_request", "missing metric id");
        if(query.from == null)
            return new FmsRspGetStats(1, "bad_request", "missing from ts");
        if(query.to == null)
            return new FmsRspGetStats(1, "bad_request", "missing to ts");
        b.add("mid", query.mid).add("from", query.from).add("to", query.to);

        if(query.base != null)  b.add("base",   query.base);
        if(query.tz != null)    b.add("tz",     query.tz);
        if(query.tf != null)    b.add("tf",     query.tf);
        if(query.limit != null) b.add("limit",  query.limit);
        if(query.page != null)  b.add("page",   query.page);
        try {
            switch (cipher){
                case AES:
                    b.add("token", get_aes_token());
                    break;
                default:
                    b.add("ps", ps);
                    break;
            }
        }catch (Exception e){
            return new FmsRspGetStats(1, "bad_request", e.getMessage());
        }

        Request.Builder req_b = new Request.Builder().url(u_get+"stats/");
        if(ka) req_b.addHeader("connection", "keep-alive");
        req_b.addHeader("accept-encoding", "deflate");

        set_http_client();
        int code;
        String msg;
        try {
            Response rsp =_http_client.newCall(req_b.post(b.build()).build()).execute();
            JSONObject jsrsp;
            code = rsp.code();
            switch(code) {
                case 200:
                    jsrsp = new JSONObject(decompress(rsp.body().bytes()));
                    return new FmsRspGetStats(code, jsrsp.getInt("next_page"), jsrsp.optJSONArray("items"));
                case 404:
                    msg = "BAD REQUEST URL";
                    break;
                case 503:
                    msg = "INTERNAL SERVER ERROR, CONTACT SUPPORT";
                    break;
                default:
                    jsrsp = new JSONObject(decompress(rsp.body().bytes()));
                    return new FmsRspGetStats(code, jsrsp.getString("status"), jsrsp.getString("msg"));
            }
        } catch (Exception e) {
            msg = e.getMessage();
            code = 0;
        }
        return new FmsRspGetStats(code, "bad_request", msg);
    }

    public static String compress(byte[] uncompressed) throws IOException {
        StringBuilder out = new StringBuilder();
        Deflater z = new Deflater(7,true);
        // z.setStrategy(Deflater.HUFFMAN_ONLY);
        z.setInput(uncompressed);
        z.finish();
        byte[] buffer = new byte[4096];
        while (!z.finished()) out.append(new String(buffer, 0, z.deflate(buffer), "UTF-8"));
        z.end();
        return out.toString();
    }
    public static String decompress(byte[] compressed) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        InflaterInputStream gis = new InflaterInputStream(bis);
        try {
            return new String(gis.readAllBytes());
        }finally {
            gis.close();
            bis.close();
        }
    }
}
