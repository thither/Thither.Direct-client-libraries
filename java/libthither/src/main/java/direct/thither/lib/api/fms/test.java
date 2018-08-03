package direct.thither.lib.api.fms;

import java.util.*;

public class test {

    public static void main(String[] args) {


        // INITIALIZE THE FLOW METRICS STATISTICS CLIENT
        FmsClient client = new FmsClient("YourFlowID");
        client.set_pass_phrase("YourPassPhrase");
        client.set_keep_alive(true);
        //client.set_version("v201807");
        client.set_cipher(FmsClient.Ciphers.AES);


        // GET AVAILABLE METRICS
        FmsRspGetDefinitions definitions = client.get_definitions(FmsDefinitionType.METRICS, null);
        if(definitions.code!=200){
            System.out.println(definitions);
            System.exit(1);
        }

        System.out.println("--------------------------------------------------------");
        System.out.println("Metrics Definitions: ");
        System.out.println("---------------------");
        int num_metrics = 0;
        for (FmsDefinition metric : definitions.metrics.values()) {
            System.out.println("Metric ID: "+metric.id);
            System.out.println("  "+metric);
            num_metrics ++;
        }
        System.out.println("---------------------");
        // CONFIGURATIONS
        Long clock_screw_adj = 0L; //
        Long from_ts = 1483228800L+clock_screw_adj; // 2017-01-01 00:00:00
        Long to_ts   = 1514764799L-clock_screw_adj; // 2017-12-31 23:59:59
        int values_range = 10;      // multiplier on hours passed plus it self
        int seconds_interval = 30;  // stat item each N seconds
        int num_items = (1+(to_ts.intValue()-from_ts.intValue())/seconds_interval)*values_range*num_metrics;
        int commit_at = 20000*values_range*num_metrics;

        System.out.println("--------------------------------------------------------");
        System.out.println("Test Configurations: ");
        System.out.println("---------------------");
        System.out.println("  From UTC ts:        "+from_ts);
        System.out.println("  To UTC ts:          "+to_ts);
        System.out.println("  Number of Items:    "+num_items);
        System.out.println("  Commit at N items:  "+commit_at);
        System.out.println("---------------------");


        // COMMON VAR TYPES
        FmsRspGetStats stats;
        FmsGetStatsQuery query;
        Map.Entry<String, Long> stats_item;
        Long value, count;
        HashMap<String, Long> metric_values;
        Long start_time;


        // GET EXISTING METRICS VALUES
        HashMap<String, HashMap<String, Long>> existing_values = new HashMap<>();
        start_time = System.currentTimeMillis();
        for (FmsDefinition metric : definitions.metrics.values()) {
            value = 0L;
            count = 0L;
            query = new FmsGetStatsQuery(metric.id, from_ts-clock_screw_adj, to_ts+clock_screw_adj);
            query.set_grouping(null,null, "%Y/%m/%d");
            do {
                stats = client.get_stats(query);
                if (stats.code == 200) {
                    while (stats.has_next()) {
                        stats_item = stats.next();
                        value+=stats_item.getValue();
                        count++;
                    }
                    query.set_page(stats.next_page);
                } else {
                    System.out.println("code:     " + stats.code);
                    System.out.println("msg_code: " + stats.msg_code);
                    System.out.println("msg:      " + stats.msg);
                    break;
                }
            } while (stats.next_page > 0);
            metric_values = new HashMap<>();
            metric_values.put("value", value);
            metric_values.put("count", count);
            existing_values.put(metric.id, metric_values);
        }
        System.out.println("--------------------------------------------------------");
        System.out.println("Existing Metrics Values: ");
        System.out.println("---------------------");
        existing_values.forEach((k,d) -> {
            System.out.println("  Metric ID: "+ k);
            d.forEach((t,v) -> System.out.println("    "+t+": "+v));
        });
        System.out.println("  Time took:  : "+ (System.currentTimeMillis()-start_time));
        System.out.println("---------------------");


        // POPULATE NEW DATA
        System.out.println("--------------------------------------------------------");
        System.out.println("Populating New Data: ");
        System.out.println("---------------------");
        HashMap<String, HashMap<String, Long>> new_values = new HashMap<>();
        for (FmsDefinition metric : definitions.metrics.values()) {
            metric_values = new HashMap<>();
            metric_values.put("value", 0L);
            metric_values.put("count", 0L);
            new_values.put(metric.id, metric_values);
        }
        Long item_ts = from_ts;
        Integer v_dif, item_value;
        List<FmsSetStatsItem> items = new ArrayList<>();
        FmsRspSetStats rsp;
        int c=0;
        do {
            v_dif = ((Long)((item_ts-from_ts)/3600)).intValue();
            for (FmsDefinition metric : definitions.metrics.values()) {
                value = 0L;
                count = 0L;
                for(int n=1;n<=values_range;n++){
                    item_value = n*(v_dif+n);
                    items.add(new FmsSetStatsItem(metric.id, item_ts, item_value.longValue()));
                    count++;
                    value+=item_value;
                    c++;
                }
                metric_values = new_values.get(metric.id);
                metric_values.put("value", metric_values.get("value")+value);
                metric_values.put("count", metric_values.get("count")+count);
                new_values.put(metric.id, metric_values);
            }
            item_ts += seconds_interval;

            if (c >= commit_at || item_ts >= to_ts){
                start_time = System.currentTimeMillis();
                rsp = client.push_list(items);

                System.out.println(
                        to_ts-item_ts
                        +", "+(System.currentTimeMillis()-start_time)
                        +", "+c
                        +", "+rsp
                        +", "+(rsp.succeed == c)
                );
                items = new ArrayList<>();
                c = 0;
            }
        } while (item_ts < to_ts);
        try{
            // 10 secs min to commit
            Thread.sleep(30000);
        }catch (Exception e){}

        // CHECK VALUES
        System.out.println("--------------------------------------------------------");
        System.out.println("Check Total Expected Value of a Metric:");
        System.out.println("  From UTC ts:        "+from_ts);
        System.out.println("  To UTC ts:          "+to_ts);
        System.out.println("---------------------");
        Long new_value;
        for (FmsDefinition metric : definitions.metrics.values()) {
            start_time = System.currentTimeMillis();
            value = 0L;
            count = 0L;
            query = new FmsGetStatsQuery(metric.id, from_ts-clock_screw_adj, to_ts+clock_screw_adj);
            query.set_grouping(null,null, "%Y/%m/%d");
            do {
                stats = client.get_stats(query);
                if (stats.code == 200) {
                    while (stats.has_next()) {
                        stats_item = stats.next();
                        value+=stats_item.getValue();
                        count++;
                    }
                    query.set_page(stats.next_page);
                } else {
                    System.out.println("code:     " + stats.code);
                    System.out.println("msg_code: " + stats.msg_code);
                    System.out.println("msg:      " + stats.msg);
                    break;
                }
            } while (stats.next_page > 0);

            new_value = new_values.get(metric.id).get("value");

            if(metric.operation.equals("avg")){
                new_value /= new_values.get(metric.id).get("count");

                if(!new_values.get(metric.id).get("count").equals(0L))
                    value = (value/count)-(
                            value-existing_values.get(metric.id).get("value"))/new_values.get(metric.id).get("count");
                else
                    value /= count;
            }else{
                value -= existing_values.get(metric.id).get("value");
            }

            System.out.println("  Metric ID: "+ metric.id + " CHECK RESULT: "+(value.equals(new_value)?"GOOD":"BAD"));
            System.out.println("    operation:  : "+ metric.operation);
            System.out.println("    count:      : "+ count);
            System.out.println("    value:      : "+ value);
            System.out.println("    expected:   : "+ new_value);
            System.out.println("    time took:  : "+ (System.currentTimeMillis()-start_time));
            System.out.println ("  -------------------");
        }
        System.exit(0);
    }
}


/*
* TEST SAMPLE OUTPUT
# java -cp ./target/libthither-0.10.3-bundled.jar direct.thither.lib.api.fms.test
--------------------------------------------------------
Metrics Definitions:
---------------------
Metric ID: 2
  FmsDefinition(Type=METRICS, id=2, name=E. Machine n.1233, desc=Electricity Consumption Machine n.1233, section=1-1-1-1-, unit_id=1, operation=sum, timezone=0, timebase=5)
Metric ID: 3
  FmsDefinition(Type=METRICS, id=3, name=E. Machine n.2221, desc=Electricity consumption E. Machine n.2221, section=2-, unit_id=1, operation=sum, timezone=0, timebase=5)
Metric ID: 4
  FmsDefinition(Type=METRICS, id=4, name=C. Machine n.1234, desc=KG Capacity . Machine n.1234, section=1-1-1-1-, unit_id=2, operation=sum, timezone=0, timebase=5)
Metric ID: 5
  FmsDefinition(Type=METRICS, id=5, name=E. Machine n.1234, desc=Electricity Consumption Machine n.1234, section=1-1-1-1-, unit_id=3, operation=avg, timezone=0, timebase=5)
Metric ID: 6
  FmsDefinition(Type=METRICS, id=6, name=E. Machine n.51234512, desc=E. Machine n.51234512, section=1-1-1-1-, unit_id=1, operation=sum, timezone=0, timebase=1440)
---------------------
--------------------------------------------------------
Test Configurations:
---------------------
  From UTC ts:        1483228800
  To UTC ts:          1514764799
  Number of Items:    52560000
  Commit at N items:  1000000
---------------------
--------------------------------------------------------
Existing Metrics Values:
---------------------
  Metric ID: 2
    count: 0
    value: 0
  Metric ID: 3
    count: 0
    value: 0
  Metric ID: 4
    count: 0
    value: 0
  Metric ID: 5
    count: 0
    value: 0
  Metric ID: 6
    count: 0
    value: 0
  Time took:  : 89
---------------------
--------------------------------------------------------
Populating New Data:
---------------------
30935999, 26942, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
30335999, 29613, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
29735999, 25135, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
29135999, 26537, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
28535999, 26240, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
27935999, 25302, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
27335999, 29600, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
26735999, 25090, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
26135999, 26458, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
25535999, 25578, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
24935999, 29791, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
24335999, 25611, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
23735999, 27951, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
23135999, 25749, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
22535999, 29919, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
21935999, 25871, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
21335999, 27470, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
20735999, 25590, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
20135999, 30317, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
19535999, 25916, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
18935999, 27804, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
18335999, 25695, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
17735999, 28343, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
17135999, 30050, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
16535999, 64186, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
15935999, 30440, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
15335999, 27021, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
14735999, 32546, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
14135999, 31451, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
13535999, 26666, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
12935999, 33462, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
12335999, 29880, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
11735999, 31237, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
11135999, 26881, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
10535999, 32564, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
9935999, 29606, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
9335999, 29810, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
8735999, 26420, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
8135999, 32330, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
7535999, 29824, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
6935999, 30234, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
6335999, 26524, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
5735999, 31696, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
5135999, 29696, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
4535999, 29802, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
3935999, 30418, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
3335999, 29939, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
2735999, 30038, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
2135999, 27182, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
1535999, 30846, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
935999, 29853, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
335999, 30664, 1000000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=1000000), true
-1, 14767, 560000, FmsRspSetStats(code=200, msg_code=OK, msg=null)(succeed=560000), true
--------------------------------------------------------
Check Total Expected Value of a Metric:
  From UTC ts:        1483228800
  To UTC ts:          1514764799
---------------------
  Metric ID: 2 CHECK RESULT: GOOD
    operation:  : sum
    count:      : 365
    value:      : 253609884000
    expected:   : 253609884000
    time took:  : 1606
  -------------------
  Metric ID: 3 CHECK RESULT: GOOD
    operation:  : sum
    count:      : 365
    value:      : 253609884000
    expected:   : 253609884000
    time took:  : 1557
  -------------------
  Metric ID: 4 CHECK RESULT: GOOD
    operation:  : sum
    count:      : 365
    value:      : 253609884000
    expected:   : 253609884000
    time took:  : 1570
  -------------------
  Metric ID: 5 CHECK RESULT: GOOD
    operation:  : avg
    count:      : 365
    value:      : 24125
    expected:   : 24125
    time took:  : 3213
  -------------------
  Metric ID: 6 CHECK RESULT: GOOD
    operation:  : sum
    count:      : 365
    value:      : 253609884000
    expected:   : 253609884000
    time took:  : 34
  -------------------

* */