# Thither.Direct - Java - Library

## INSTALL
###### BUILDING JAR 

    git clone git://github.com/thither/Thither.Direct-client-libraries.git; 
    cd Thither.Direct-client-libraries/java; 
    mvn -f pom.xml -Dmaven.test.skip=true package;
The command will create a library and a bundled library at ./target
with files libthither-0.10.3-bundled.jar and libthither-0.10.3.jar

running test and examples:
* edit the examples.java and test.java with your FlowID configurations
* run
  + java -cp ./target/libthither-0.10.3-bundled.jar direct.thither.lib.api.fms.test
  + java -cp ./target/libthither-0.10.3-bundled.jar direct.thither.lib.api.fms.examples

###### READY JAR 
    download https://github.com/thither/Thither.Direct-client-libraries/releases/download/v0.10.3/libthither-0.10.3.jar

###### MAVEN BUILD WITH REPOSITORY
Add to pom.xml the repository and a dependency:
```xml
    <repositories>
        <repository>
            <id>thither.direct</id>
            <name>Thither.Direct public repository</name>
            <url>https://thither.direct/static/client_libraries/java/mvn/repo/</url>
        </repository>
    </repositories>
	
    <dependencies>
        <dependency>
            <groupId>direct.thither</groupId>
            <artifactId>libthither</artifactId>
            <version>0.10.3</version>
        </dependency>
    </dependencies>
```
##### DEPENDENCIES
###### required:
     org.json
     com.squareup.okhttp3
     org.bouncycastle
     
     
  
## USING THE LIBRARY
+ The APIs are:
  + Flow Metrics Statistics Client
    + push_single
    + push_list
    + push_csv_data
    + get_definitions
    + get_stats
  + (more to come)
+ Utils: 
  + (still to come)

### API - Flow Metrics Statistics Client
After you have created your [Flow Metrics Statistics account](https://thither.direct/information/services/commercial/analytic_services/fms) and have your FlowID 

#### Initiating the Flow Metrics Statistics client
```java
import direct.thither.lib.api.fms;

FmsClient client = new FmsClient("YourFlowId");
client.set_keep_alive(true);
client.set_pass_phrase("YourPassPhrase");
//client.set_cipher(FmsClient.Ciphers.AES);
//client.version("v201807");
//client.set_https(false);
```
option to set the OkHttpClient instance configurations such as proxy or specific timeouts
```java
OkHttpClient http_client = client.get_http_client();

```

### PUSHING/POSTING FLOW STATISTICS DATA

#### A FMS SET STATS ITEM
FmsSetStatsItem class is for posting methods input.
It has the following over-loaders,
for initializing in full:
```java
FmsSetStatsItem(String metric_id, String date_time, String value);
FmsSetStatsItem(String metric_id, String date_time, Long value);
FmsSetStatsItem(String metric_id, long date_time, String value);
FmsSetStatsItem(String metric_id, long date_time, Long value);

```
for partial initialization:
```java
FmsSetStatsItem item = new FmsSetStatsItem(String metric_id);
item.set_details(String date_time, String value);
item.set_details(String date_time, Long value);
item.set_details(long date_time, String value);
item.set_details(long date_time, Long value);

```
for item with error initialization:
```java
FmsSetStatsItem(String error, String metric_id, String date_time, String value)
```
FmsRspSetStats has the following symbols:
* mid: String, Metric ID
* dt:  String, Unix Timestamp in seconds or Formatted DateTime
* v:   String, Value +/-/= number
* e:   String, corresponding message on error


#### A FMS SET STATS RESPONSE
FmsRspSetStats class is the returned object for post methods.
It has the following symbols:
* code:     int,                    the server's http response code or zero for prior request errors
* msg_code: String,                 for 200 code OK/BAD/SOME_ERRORS , for other codes the error
* msg:      String,                 describing message (on none 200 codes)
* succeed:  int,                    number of items succeed
* failed:   int,                    number of items failed
* errors:   List<FmsSetStatsItem>,  a list of FmsSetStatsItem objects


##### PUSHING SINGLE ITEM
```java
FmsRspSetStats r = client.push_single(item);
```
+ Parameters
  + item: FmsSetStatsItem, Initialized FmsSetStatsItem object
+ Returns
  + FmsRspSetStats object


##### PUSHING MULTIPLE ITEMS - push_list
```java
FmsRspSetStats r = client.push_list(items);
```
+ Parameters
  + items: List<FmsSetStatsItem>, a list of FmsSetStatsItem objects
+ Returns
  + FmsRspSetStats object

##### PUSHING MULTIPLE ITEMS - push_csv_data
```java
FmsRspSetStats r = client.push_csv_data(csv_data);
```
+ Parameters
  + csv_data: String, a csv format data, header mid,dt,v
+ Returns
  + FmsRspSetStats object


### GETTING FLOW DATA

##### GETTING DEFINITIONS DATA - all/units/sections/metrics
```java
FmsRspGetDefinitions definitions = client.get_definitions(type, HashMap<String,String> query);
```
+ Parameters
  + type:   enum FmsDefinitionType, options ALL, UNITS, SECTIONS, METRICS
  + query:  HashMap<String,String>, optionally null
            +  section :    str,   Only on this section level, apply only to sections and metrics types
            +  unit :       str,   Only metrics with this Unit ID
            +  operation :  str,   Only metrics that timebase join operation is sum/avg
            +  timebase :   str,   Only metrics that with this timebase(minutes)
+ Returns
  + FmsRspGetDefinitions object

FmsRspGetDefinitions object has the following symbols:
* code:     int,                    the server's http response code or zero for prior request errors
* msg_code: String,                 the error code for none 200 code
* msg:      String,                 describing message (on none 200 codes)
* units:    ConcurrentHashMap<String, FmsDefinition> ,  Map of FmsDefinition objects
* sections: ConcurrentHashMap<String, FmsDefinition> ,  Map of FmsDefinition objects
* metrics:  ConcurrentHashMap<String, FmsDefinition> ,  Map of FmsDefinition objects

FmsDefinition object has the following symbols:
+ String id, name                           :  apply to all
+ String desc                               :  apply to sections and metrics
+ String operation, section_id, unit_id     :  apply metrics
+ Integer timezone, timebase                :  apply metrics
+ FmsDefinitionType type                    :  apply to all


usage example, print everything:
```java
FmsRspGetDefinitions definitions = client.get_definitions(FmsDefinitionType.ALL, null);
System.out.println(definitions);
```

##### GETTING STATISTICS DATA

```java
FmsRspGetStats stats = client.get_stats(query);
```
+ Parameters
  + query: FmsGetStatsQuery, stats query object
+ Returns
  + FmsRspGetStats object

The FmsGetStatsQuery object:
+ initialization FmsGetStatsQuery(String metric_id, Long from_ts, Long to_ts)
  + ts in seconds
+ setters:
  + set_grouping(Integer minutes_base, Integer minutes_timezone, String time_format)
  + set_limit(Integer l) , max 1,000,000 default 100,000
  + set_page(Integer p)  , default first page

The FmsRspGetStats object:
+ It has the following symbols:
   * code:     int,                    the server's http response code or zero for prior request errors
   * msg_code: String,                 the error code for none 200 code
   * msg:      String,                 describing message (on none 200 codes)
   * next_page: int,  next_page available or zero for none
   * items:     JSONArray, the items object in it's original http response object
+ The follow methods are available:
   * Map.Entry<String, Long> next(),    get next item map <datetime, value>

   * boolean has_next(),                whether there is a next item.


usage example, printing out all stats of a MetricId in timestamp range
```java
        FmsRspGetStats stats;
        Map.Entry<String, Long> stats_item;
        FmsGetStatsQuery query = new FmsGetStatsQuery("YourMetricID", FROM_SECS_TS, TO_SECS_TS);
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
```


####  RESPONSES to calls with Flow Metrics Statistics client methods

##### API responds with a HTTP status-code and with application/json status data for request made to the server

###### Bad API request, Response syntax:

     status-code: 400
     {"status": "bad_request", "msg": MESSAGE}

+ messages and description:
  + access_detail - missing access parameters
  + not_supported_compression - compression used is not supported
  + bad_csv_header - wrong csv data header
  + item_missing - found zero items
  + missing_param: +field - item is missing a parameter
  + bad_definition_type: definition type requested is not available
  + bad_mid_param: MetricId is missing in a request
  + bad_timestamps: timestamps are not number or from is above to
  + bad_time_format: requested time-format is not optional
  + bad_param: either tz/base/limit/page is not a number

###### Bad API auth request, Response syntax:

     status-code: 401
     {"status": "unauthorized", "msg": MESSAGE}

+ messages and description:
  + ip_blocked - ip address is blocked, open a support ticket
  + api_access - missing access parameters
  + flow_acc_expired - Flow Metrics Statistics account has expired
  + encryption_mismatch - digest/nonce mismatch

###### Bad API auth request, Response syntax:

     status-code: 404
     {"status": "unauthorized", "msg": MESSAGE}

+ messages and description:
  + no_method - bad url path
 
###### Successful API request, Response syntrax:

    status-code: 200
    {'status': 'OK', 'succeed': Number_Of_Items}
        
###### Partially Successful API request, Response syntax:

    status-code: 200
    {'status': 'SOME_ERRORS', 'succeed': Number_Of_Items, 'failed': Number_Of_Items, 'errors': [Errors_and_Corresponding_Items]}
   
###### Failed API request, Response syntax:

    status-code: 200
    {'status': 'BAD', 'failed': Number_Of_Items, 'errors': [Errors_and_Corresponding_Items]}
    
    
##### Errors and Corresponding Items
respond's 'errors' key, a list of items with the error and the corresponding item

    [[error, mid, dt, v],]

+ errors and description:
  + bad_value:                  Value is not -/=/+(number)
  + bad_time_format:            data and time is not in %Y-%m-%d %H:%M:%S format
  + no_such_metric_id:          the metric Id does not exists
  + bad_time_future:            future time is not allowed
  + bad_time_prior_acc_active:  12 weeks prior account creation is not allowed


#### API EXAMPLES - Flow Metrics Statistics Client
Java libthither
 [examples.java](libthither/src/main/java/direct/thither/lib/api/fms/examples.java) 
 and 
 [test.java](libthither/src/main/java/direct/thither/lib/api/fms/test.java)







            
