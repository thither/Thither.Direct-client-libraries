# Thither.Direct - Python - Library

## INSTALL
   
    pip install https://github.com/kashirin-alex/Thither.Direct-client-libraries/archive/libthither-v0.10.0.tar.gz
      or (as availble) 
    pip install libthither
or to copy the content of (libthither) to your application     
    
##### DEPENDENCIES
###### required:
     pip install requests Cryptodome zlib
###### optional:
     pip install brotli
     
     
  
## USING THE LIBRARY

### API Client for Flow Metrics Statistics

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
                optional depends on Flow configurations
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

#### A FLOW METRICS STATS ITEM
Depends on the method used to push the item, while the item definition format remain the same.
+ MetricId: It is a Metric ID, a user has created on Thither.Direct for a given FlowID
+ DateTime: Format '%Y-%m-%d %H:%M:%S' unless otherwise specified on the metric configurations
+ Value:    String/Integer - Positive, Negative or =Equal, It is the value tracked on the specific time for a metric


##### PUSHING SINGLE ITEM
```python
client.push_single(mid, dt, v)
```
            Parameters
            ----------
            mid : str
                MetricId
            dt : str
                DateTime
            v : str/int
                Value

            Returns
            -------
            requests lib response
            or an rsp object with status_code and content {'status': 'bad_request', 'error': desc}


##### PUSHING MULTIPLE ITEMS - push_list
```python
client.push_list([[MetricId, DateTime, Value],])
```
            Parameters
            ----------
            items : list
                list of items [[MetricId, DateTime, Value],]
            Returns
            -------
            requests lib response
            or an rsp object with status_code and content {'status': 'bad_request', 'error': desc}


##### PUSHING MULTIPLE ITEMS - push_csv_data
```python
client.push_csv_data("mid,dt,v\n"+"MetricId,DateAndTime,Value")
```
            Parameters
            ----------
            csv_data : str
                a csv data with 'mid', 'dt', 'v' columns
            Returns
            -------
            requests lib response
            or an rsp object with status_code and content {'status': 'bad_request', 'error': desc}


####  RESPONSES of calls with Flow Metrics Statistics client

##### Errors originated prior a request to the server has the follow error-codes and description:

    0: 'param_empty',
    1: 'list_empty',
    2: 'csv_data_empty',
    3: 'bad_csv_header',
    4: 'bad_csv_row',
    5: 'flow_id_is_required',
        
###### Bad API syntrax:
    
    status-code: error-code
    {'status': "bad_request", 'error': "errors-code-desc}
        
        
##### For request made to the server, API responds with a HTTP status-code and with application/json status data 

###### Bad API syntrax:

    status-code: 400
    {'status': "bad_request"}
    
###### Bad API login (flowId/passphrase/ip-address are not authorized):

    status-code: 401
    {'status': "unauthorized"}
    
###### Bad API login (digest/nonce mismatch):

    status-code: 401
    {'status': "unauthorized_digest_mismatch"}
    
###### Bad API syntrax, csv data header:

    status-code: 400
    {'status': "bad_csv_header"}
    
###### Bad API syntrax, found zero items:

    status-code: 400
    {'status': "bad_request_empty"}
    
###### Bad API syntrax, missing a field:

    status-code: 400
    {'status': "bad_request", 'missing': The_Missing_ParameterField}
   
###### Succesfull request:

    status-code: 200
    {'status': 'OK', 'succeed': Number_Of_Items}
        
###### Partially Succesfull request:

    status-code: 200
    {'status': 'SOME_ERRORS', 'succeed': Number_Of_Items, 'failed': Number_Of_Items, 'errors': [Corresponding_Errors_and_Items]}
   
###### Failed request:

    status-code: 200
    {'status': 'BAD', 'failed': Number_Of_Items, 'errors': [Corresponding_Errors_and_Items]}
    
    
##### Errors and Corresponding Items
'errors' key, a list of items with the error andf the corresponding item

    [[error, mid, dt, v],]

errors: 
+ bad_value:           Value is not -/=/+(number)
+ bad_time_format:     data and time is not in %Y-%m-%d %H:%M:%S format
+ no_such_metric_id:   the metric Id does not exists
+ bad_time_future:     future time is not allowed



#### Flow Metrics Statistics Client - API EXAMPLES
Python libthither [examples-api-fms-client.py](examples-api-fms-client.py)





            
