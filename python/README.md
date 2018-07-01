# Thither.Direct - Python - Library

## INSTALL
   
    pip install https://github.com/kashirin-alex/Thither.Direct-client-libraries/archive/libthither-v0.10.0.tar.gz
      or
    pip install libthither
      or 
    copy the folder https://github.com/kashirin-alex/Thither.Direct-client-libraries/tree/master/python/libthither 
    
##### DEPENDENCIES
###### required:
     pip install requests pycryptodomex 
###### optional:
     pip install brotli
     
     
  
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
```python
from libthither.api.fms import FlowMetricsStatisticsClient as FmsClient

client = FmsClient('YourFlowID',
                   # pass_phrase='YourPassPhrase',
                   # cipher='AES',
                   keep_alive=True)
```
            Parameters
            ----------
            fid : str
                Your FlowID

            Keyword Args
            ----------
            pass_phrase : str
                Your API pass-phrase,
                optional depends on Flow configurations and not required with allowed IP address are set
            https : bool
                Whether to use https,
                default True
            cipher : str
                Authentication Cipher AES(the only available for now),
                optional depends on Flow configurations
            keep_alive : bool
                Whether to keep session alive,
                default False
            json : bool
                Whether to use only JSON Content-Type,
                default False
            requests_args : dict
                Passed kwargs to 'requests' library

            Returns
            -------
            FlowMetricsStatisticsClient instance
For a not based on libthither, you can check-on [api-without-pkg] guide.

### PUSHING/POSTING FLOW STATISTICS DATA

#### A FLOW METRICS STATS ITEM
Depends on the method used to push the item, while the item definition format remain the same.
+ MetricId: It is a Metric ID, a user has created on Thither.Direct for a given FlowID
+ DateTime: Format '%Y-%m-%d %H:%M:%S' unless otherwise specified on the metric configurations
+ Value:    String/Integer - Positive, Negative or =Equal, It is the value tracked on the specific time for a metric


##### PUSHING SINGLE ITEM
```python
client.push_single(mid, dt, v)
```
+ Parameters
  + mid: str, MetricId
  + dt: str,  DateTime
  + v: str/int,   Value
+ Returns
  + 'requests' lib response
  + or an rsp object with status_code and content {'status': 'bad_request', 'error': desc}


##### PUSHING MULTIPLE ITEMS - push_list
```python
client.push_list([[MetricId, DateTime, Value],])
```
+ Parameters
  + items: list, a list of items [[MetricId, DateTime, Value],]
+ Returns
  + 'requests' lib response
  + or an rsp object with status_code and content {'status': 'bad_request', 'error': desc}


##### PUSHING MULTIPLE ITEMS - push_csv_data
```python
client.push_csv_data("mid,dt,v\n"+"MetricId,DateAndTime,Value")
```
+ Parameters
  + csv_data: str, a csv data with 'mid', 'dt', 'v' columns
+ Returns
  + 'requests' lib response
  + or an rsp object with status_code and content {'status': 'bad_request', 'error': desc}

### GETTING FLOW STATISTICS DATA

##### GETTING DEFINITIONS DATA - all/units/sections/metrics
```python
client.get_definitions(DEFINITION_TYPE, **kwargs)
```
+ Arguments
  + DEFINITION_TYPE: str, Type of Definition units/sections/metrics , nothing or empty string for all

+ Keyword Args
  +  section : str,        Only on this section level, apply only to sections and metrics types
  +  unit : str,           Only metrics with this Unit ID
  +  operation : str,      Only metrics that timebase join operation is sum/avg
  +  timebase : str/int,   Only metrics that with this timebase(minutes)
  
+ Returns
  + 'requests' lib response
  + with JSON content of a dict{DEFINITION_TYPE: {TYPE_ID: {INFO_NAME: VALUE}}
  + or an rsp object with status_code and content {'status': 'bad_request', 'error': desc}
  
usage example:
```python
rsp = client.get_definitions()
if rsp.status_code == 200:
    js_rsp = rsp.json()
    for typ in ['units', 'sections', 'metrics']:
        print (js_rsp[typ])
else:
    print (rsp.status_code)
    print (rsp.content)
```


####  RESPONSES to calls with Flow Metrics Statistics client methods

###### Errors originated prior a request to the server, Bad API Request syntrax:

    status-code: error-code
    {"status": "bad_request", "msg": MESSAGE}
    
+ error-codes and messages:
  + 0: 'param_empty',
  + 1: 'list_empty',
  + 2: 'csv_data_empty',
  + 3: 'bad_csv_header',
  + 4: 'bad_csv_row',
  + 5: 'flow_id_is_required',
        
##### API responds with a HTTP status-code and with application/json status data for request made to the server

###### Bad API request, Response syntrax:

     status-code: 400
     {"status": "bad_request", "msg": MESSAGE}

+ messages and description:
  + access_detail - missing access parameters
  + not_supported_compression - comperssion used is not supported
  + bad_csv_header - wrong csv data header
  + item_missing - found zero items
  + missing_param: +field - item is missing a parameter

###### Bad API auth request, Response syntrax:

     status-code: 401
     {"status": "unauthorized", "msg": MESSAGE}

+ messages and description:
  + ip_blocked - ip address is blocked, open a ticket
  + encryption_mismatch - digest/nonce mismatch
  + api_access - missing access parameters
  + flow_acc_expired - Flow Metrics Statistics account has expired

###### Succesfull API request, Response syntrax:

    status-code: 200
    {'status': 'OK', 'succeed': Number_Of_Items}
        
###### Partially Succesfull API request, Response syntrax:

    status-code: 200
    {'status': 'SOME_ERRORS', 'succeed': Number_Of_Items, 'failed': Number_Of_Items, 'errors': [Errors_and_Corresponding_Items]}
   
###### Failed API request, Response syntrax:

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
Python libthither [examples-api-fms-client.py](examples-api-fms-client.py)





            
