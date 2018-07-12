package direct.thither.lib.api.fms;
/*
 * Author Kashirin Alex (kashirin.alex@gmail.com)
 * THITHER.DIRECT
 * */

import java.util.*;

public class examples {

    public static FmsClient client;
    public static TimeZone tz = TimeZone.getTimeZone("UTC");

    public static void main(String[] args) {

        // INITIALIZE THE FLOW METRICS STATSTICS CLIENT
        client = new FmsClient("YourFlowId");
        client.set_keep_alive(true);
        client.set_pass_phrase("YourPassPhrase");
        //client.set_cipher(FmsClient.Ciphers.AES);

        // EXAMPLES, PUSHING SINGLE ITEM
        FmsRspSetStats r;

        // client.push_single, FmsSetStatsItem
        for(int n=0; n<10;n++) {
            r = client.push_single(new FmsSetStatsItem("2", Calendar.getInstance(tz).getTimeInMillis(), 123+n));
            System.out.println("code:     "+r.code);
            if(r.code == 200) {
                System.out.println("succeed:  "+r.succeed);
                System.out.println("failed:   "+r.failed);
                System.out.println("errors:   "+r.errors);
            } else {
                System.out.println("msg_code: "+r.msg_code);
                System.out.println("msg:      "+r.msg);
            }
        }
        // client.push_single, FmsSetStatsItem with Metric ID preset
        FmsSetStatsItem sm = new FmsSetStatsItem("2");
        for(int n=0; n<10;n++) {
            sm.set_details(Calendar.getInstance(tz).getTimeInMillis(), 123+n);
            r = client.push_single(sm);
            System.out.println("code:     "+r.code);
            if(r.code == 200) {
                System.out.println("succeed:  "+r.succeed);
                System.out.println("failed:   "+r.failed);
                System.out.println("errors:   "+r.errors);
            } else {
                System.out.println("msg_code: "+r.msg_code);
                System.out.println("msg:      "+r.msg);
            }
        }

        // EXAMPLES, PUSHING SEVERAL ITEMS
        // client.push_list in groups of metric id
        List<FmsSetStatsItem> items;
        FmsSetStatsItem stat_item;
        for(int mid=2; mid<7; mid++) {
            items = new ArrayList<>();
            stat_item = new FmsSetStatsItem(String.valueOf(mid));
            for(int n=0; n<100000;n++){
                stat_item.set_details(Calendar.getInstance(tz).getTimeInMillis(), 1+n);
                items.add(stat_item);
            }
            r = client.push_list(items);
            System.out.println("code:     "+r.code);
            if(r.code == 200) {
                System.out.println("succeed:  "+r.succeed);
                System.out.println("failed:   "+r.failed);
                System.out.println("errors:   "+r.errors);
            } else {
                System.out.println("msg_code: "+r.msg_code);
                System.out.println("msg:      "+r.msg);
            }
        }

        // client.push_csv_data in groups of metric id
        StringBuilder csv_data;
        long ts;
        for(int mid=2; mid<7; mid++) {
            csv_data = new StringBuilder("mid,dt,v\n");
            for(int n=0; n<100000;n++){
                ts = Calendar.getInstance(tz).getTimeInMillis();
                csv_data.append(new FmsSetStatsItem(String.valueOf(mid), ts, 1+n).to_csv_line());
            }
            r = client.push_csv_data(csv_data.toString());
            System.out.println("code:     "+r.code);
            if(r.code == 200) {
                System.out.println("succeed:  "+r.succeed);
                System.out.println("failed:   "+r.failed);
                System.out.println("errors:   "+r.errors);
            } else {
                System.out.println("msg_code: " + r.msg_code);
                System.out.println("msg:      " + r.msg);
            }
        }


        // client.push_csv_data in groups of metric id
        FmsRspGetStats stats;
        Map.Entry<String, Integer> stats_item;
        FmsGetStatsQuery query = new FmsGetStatsQuery("2", 1499228800L, 1514764679L);
        query.set_limit(10000);
        do {
            stats = client.get_stats(query);
            System.out.println("code:     "+stats.code);
            if(stats.code == 200){
                while (stats.has_next()){
                    stats_item = stats.next();
                    System.out.println(stats_item.getKey() + ": " + stats_item.getValue());
                }
                System.out.println("next_page: " + stats.next_page);
                query.set_page(stats.next_page);
            } else {
                System.out.println("msg_code: " + stats.msg_code);
                System.out.println("msg:      " + stats.msg);
                break;
            }
        } while (stats.next_page>0);

        FmsRspGetDefinitions definitions = client.get_definitions(FmsDefinitionType.ALL, null);
        System.out.println(definitions);


    }

}
