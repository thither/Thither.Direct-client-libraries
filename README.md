# Thither.Direct - API - FLOW METRICS STATISTICS 
On this branch API Guide to Flow Metrics Statistics and exmaples of usage 

In order to add metric data from extrenal application to your FlowID at Thither.Direct an API is available.
The API's root url https://thither.direct/api/fms/post/
The options to make requests to the server are urlencoded(GET, POST), 

### Single Item - variations for posting metrics data

#### urlencoded(GET, POST) 
  
  Rquests Parameters:

    fid    # Your Flow Metrics Statistics ID
    ps     # Your Pass-Phrase
    mid    # Metric ID
    dt     # Metric's Date and Time, format %Y-%m-%d %H:%M:%S
    v      # Metric's value positive, negative or =equal
    
A url with the urlecoded parameters can be used to put metric data to your flow via the GET method on your browser

    https://thither.direct/api/fms/post/?fid=YourFlowID&ps=YourPassPhrase&mid=YourMetricID&dt=%Y-%m-%d%20%H:%M:%S:11&v=MetricValue
