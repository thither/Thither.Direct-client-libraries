# Thither.Direct - API - FLOW METRICS STATISTICS 
On this branch API Guide to Flow Metrics Statistics and exmaples of usage 

An API is available, in order to add metrics data from extrenal application to your FlowID on Thither.Direct

## API's CLIENT SETTINGS
API's root-url https://thither.direct/api/fms/post/

Options for requests to the API's root-url are seperated by SINGLE ITEM or MULTIPLE ITEMS.

### options for SINGLE ITEM 

#### GET or POST - parameters urlencoded 
  
  Parameters for a Request:

    fid    # Your Flow Metrics Statistics ID
    ps     # Your API Pass-Phrase
    mid    # Metric ID
    dt     # Metric's Date and Time, format %Y-%m-%d %H:%M:%S
    v      # Metric's Value(positive, negative or =equal)
    
A url with the urlecoded parameters can be used to put metric data to your flow via the GET method on your browser

    https://thither.direct/api/fms/post/?fid=YourFlowID&ps=YourPassPhrase&mid=YourMetricID&dt=%Y-%m-%d%20%H:%M:%S:11&v=MetricValue

#### POST - parameters json encoded
  
  JSON data for a Request:
  
      {
         'fid': 'YourFlowID',            # Your Flow Metrics Statistics ID
         'ps':  'YourPassPhrase',        # Your API Pass-Phrase
         'mid': 'YourMetricID',          # Metric ID
         'dt':  '%Y-%m-%d %H:%M:%S',     # Metric's Date and Time, format 
         'v':    Integer                 # Value(positive, negative or =equal)
      }
    
    
### options for MULTIPLE ITEMS

#### POST - parameters and a csv file

  Parameters for a Request:

    fid    # Your Flow Metrics Statistics ID
    ps     # Your API Pass-Phrase

  Request with a File, filename has to be 'csv'
  CSV format, 1st row as header with columns mid,dt,v
     
     mid,dt,v
     1,2018-06-12 13:04:11,1234
     2,2018-06-12 13:04:11,121
     2,2018-06-12 14:04:11,122
     2,2018-06-12 14:14:11,123

 #### POST -  json encoded parameters and a csv data 
  
  JSON data for a Request:
  
      {
         'fid': 'YourFlowID',            # Your Flow Metrics Statistics ID
         'ps':  'YourPassPhrase',        # Your API Pass-Phrase
         'csv':  csv_data                # CSV format the same as posting a csv file
      }

  

  
  
  
