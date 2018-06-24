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

            client.push_single(YourMetricId, DateAndTime, Value)
            client.push_list([[Metric ID, DateTime, Value],])
            client.push_csv_data("mid,dt,v\nYourMetricId,DateAndTime,Value")
