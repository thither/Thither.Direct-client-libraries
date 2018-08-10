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

        // INITIALIZE THE FLOW METRICS STATISTICS CLIENT
        client = new FmsClient("YourFlowID");
        client.set_pass_phrase("YourPassPhrase");
        client.set_keep_alive(true);
        //client.set_version("v201807");
        client.set_cipher(FmsClient.Ciphers.AES);

        // client.get_queue thread per metric id
        System.out.println("new FmsSetStatsQueue, thread per metric id, example start");

        final FmsSetStatsQueue q = new FmsSetStatsQueue(client, 2000, 300);
        q.set_callbacks(new FmsSetStatsQueue.FmsSetStatsCallBack() {
            @Override
            public void onRspStats(FmsRspSetStats rsp) {
                System.out.println("CB "+rsp);
                System.out.println("CB queued: "+q.queued());
            }
            @Override
            public void onQueueStop() {
                System.out.println("CB onQueueStop ");
                System.out.println("CB queued: "+q.queued());
            }
        });
        List<Thread> threads = new ArrayList<>();
        for(int mid=2; mid<7; mid++) {
            final String metric_id = String.valueOf(mid);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int n1=0; n1<100;n1++){
                        for(int n2=0; n2<1000;n2++){
                            q.add(new FmsSetStatsItem(metric_id,  Calendar.getInstance(tz).getTimeInMillis(),
                                    ((Integer)(1+n1*n2)).longValue()));
                        }
                        try{ Thread.sleep(888);}
                        catch (Exception e){}
                    }
                }
            });
            t.start();
            threads.add(t);
        }
        for (int i=0; i<threads.size(); i++) {
            try {
                System.out.println("Waiting for thread: " + i);
                threads.get(i).join();
            }catch (Exception e){}
        }
        while (q.queued()>0){
           try{ Thread.sleep(3000);}
           catch (Exception e){}
            System.out.println("q waiter queued: "+q.queued());
        }
        q.finalize_and_stop();

        try{ Thread.sleep(3000);}catch (Exception e){}
        System.out.println("client.get_queue, thread per metric id, example end");

        // EXAMPLES, PUSHING SINGLE ITEM
        FmsRspSetStats r;

        // client.push_single, FmsSetStatsItem
        System.out.println("client.push_single, FmsSetStatsItem, example start");
        for(int n=0; n<10;n++) {
            r = client.push_single(
                    new FmsSetStatsItem("2",
                            Calendar.getInstance(tz).getTimeInMillis(),
                            ((Integer)(123+n)).longValue())
            );
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
        System.out.println("client.push_single, FmsSetStatsItem, example end");

        // client.push_single, FmsSetStatsItem with Metric ID preset
        System.out.println("client.push_single, FmsSetStatsItem with Metric ID preset, example start");
        FmsSetStatsItem sm = new FmsSetStatsItem("2");
        for(int n=0; n<10;n++) {
            sm.set_details(Calendar.getInstance(tz).getTimeInMillis(), ((Integer)(123+n)).longValue());
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
        System.out.println("client.push_list in groups of metric id, example start");
        // client.push_list in groups of metric id
        List<FmsSetStatsItem> items;
        for(int mid=2; mid<7; mid++) {
            items = new ArrayList<>();
            for(int n=0; n<100000;n++){
                items.add(new FmsSetStatsItem(String.valueOf(mid),
                        Calendar.getInstance(tz).getTimeInMillis(), ((Integer)(1+n)).longValue()));
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
        System.out.println("client.push_list in groups of metric id, example end");

        // client.push_csv_data in groups of metric id
        System.out.println("client.push_csv_data in groups of metric id, example start");
        StringBuilder csv_data;
        long ts;
        for(int mid=2; mid<7; mid++) {
            csv_data = new StringBuilder("mid,dt,v\n");
            for(int n=0; n<100000;n++){
                ts = Calendar.getInstance(tz).getTimeInMillis();
                csv_data.append(
                        new FmsSetStatsItem(String.valueOf(mid), ts, ((Integer)(1+n)).longValue()).to_csv_line());
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
        System.out.println("client.push_csv_data in groups of metric id, example end");

        // client.get_stats
        System.out.println("client.get_stats, example start");
        FmsRspGetStats stats;
        Map.Entry<String, Long> stats_item;
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
        System.out.println("client.get_stats, example start");

        System.out.println("client.get_definitions, example start");
        FmsRspGetDefinitions definitions = client.get_definitions(FmsDefinitionType.ALL, null);
        System.out.println(definitions);
        System.out.println("client.get_definitions, example end");

        System.exit(0);

    }

}
