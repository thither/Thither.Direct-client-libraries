# Thither.Direct - Python - Library

## INSTALL
   
    pip install https://github.com/kashirin-alex/Thither.Direct-client-libraries/releases/download/libthither-v0.10.0/libthither-0.10.0.tar.gz
    
 or 
 
     pip install libthither
  
## USING THE LIBRARY

### API Client for Flow Metrics Statistics

#### Initiating the client
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


            
