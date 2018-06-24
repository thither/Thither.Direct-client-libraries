# Thither.Direct - Python - Library

## INSTALL
   
    pip install https://github.com/kashirin-alex/Thither.Direct-client-libraries/releases/download/libthither-v0.10.0/libthither-0.10.0.tar.gz
    
 or 
 
     pip install libthither
  

## USAGING THE LIBRARY

```python
            from libthither.api.fms import FlowMetricsStatisticsClient
            client = FlowMetricsStatisticsClient(
                        'YourFlowID',
                        pass_phrase='YourPassPhrase',
                        https=True,
                        cipher='',
                        keep_alive=True,
                        json=False
                        requests_args={},
                        )
            after initiated, client can be called with:
            client.push_single(YourMetricId, DateAndTime, Value)
            client.push_list([[Metric ID, DateTime, Value],])
            client.push_csv_data("mid,dt,v\nYourMetricId,DateAndTime,Value")
```
