# -- coding: utf-8 --

# # #  FLOW METRICS STATISTICS - API THITHER.DIRECT - PYTHON CLIENT # # #

import requests
import datetime
dt_now = datetime.datetime.now()


# # # EXAMPLES FOR POSTING SINGLE ITEM # # #


# GET urlencoded
print ('*'*80)
print ('GET URLENCODED')
rsp = requests.get('https://thither.direct/api/fms/post/',
                   params={
                       'fid': 'YourFlowID',                          # Flow Metrics Statistics ID
                       'ps':  'optionalYourPassPhrase',              # API pass-phrase
                       'mid': 'YourMetricID',                        # Metric ID
                       'dt':  dt_now.strftime('%Y-%m-%d %H:%M:%S'),  # Date and Time
                       'v':   '123'                                   # value positive, negative or =equal
                   })
print (rsp.status_code)
print (rsp.content)
print ('END GET URLENCODED')
print ('')
#


# POST urlencoded
print ('*'*80)
print ('POST URLENCODED')
rsp = requests.post('https://thither.direct/api/fms/post/',
                    headers={'Content-Type': 'application/x-www-form-urlencoded'},
                    params={
                        'fid': 'YourFlowID',                          # Flow Metrics Statistics ID
                        'ps':  'optionalYourPassPhrase',              # API pass-phrase
                        'mid': 'YourMetricID',                        # Metric ID
                        'dt':  dt_now.strftime('%Y-%m-%d %H:%M:%S'),  # Date and Time
                        'v':   '123'                                   # value positive, negative or =equal
                    })
print (rsp.status_code)
print (rsp.content)
print ('END POST URLENCODED')
print ('')
#


# POST JSON
print ('*'*80)
print ('POST JSON')
rsp = requests.post('https://thither.direct/api/fms/post/',
                    json={
                        'fid': 'YourFlowID',                          # Flow Metrics Statistics ID
                        'ps':  'optionalYourPassPhrase',              # API pass-phrase
                        'mid': 'YourMetricID',                        # Metric ID
                        'dt':  dt_now.strftime('%Y-%m-%d %H:%M:%S'),  # Date and Time
                        'v':   '123'                                   # value positive, negative or =equal
                    })
print (rsp.status_code)
print (rsp.content)
print ('END POST JSON')
print ('')
#


# # # EXAMPLES FOR POSTING MULTIPLE ITEMS # # #


# GENERATED EXAMPLE ITEMS DATA
items = [[mid, (dt_now - datetime.timedelta(days=d, minutes=m, seconds=s)).strftime('%Y-%m-%d %H:%M:%S'), c]
         for mid in range(1, 6)            # 5 metric IDs range
         for s in range(0, 60, 30)         # seconds in minute / twice a minute
         for m in range(0, 3600)           # minutes in 1 day
         for d in range(0, 28)             # 28 days
         for c in range(1000, 3000, 1000)  # 2x values once at 0sec second at 30sec
         ]
print ('Num Items:' + str(len(items)))
# 2016000 (items) , value(for a 5 minute time frame = 5x2x(1000+3000))


# CSV EXAMPLE DATA
csv_data = "mid,dt,v\n"
for item in items:
    csv_data += ','.join([str(v) for v in item])+"\n"
print ('csv_data size:' + str(len(csv_data)))
#

# POST csv data - multipart encoded
print ('*'*80)
print ('POST csv data - multipart encoded')
rsp = requests.post('https://thither.direct/api/fms/post/',
                    params={
                        'fid': 'YourFlowID',                # Flow Metrics Statistics ID
                        'ps':  'optionalYourPassPhrase',    # API pass-phrase
                    },
                    files={'csv': csv_data})
print (rsp.status_code)
print (rsp.content)
print ('END POST csv data - multipart encoded')
print ('')
#


# POST csv data - JSON encoded
print ('*'*80)
print ('POST csv data - JSON encoded')
rsp = requests.post('https://thither.direct/api/fms/post/',
                    json={
                        'fid': 'YourFlowID',                  # Flow Metrics Statistics ID
                        'ps':  'optionalYourPassPhrase',      # API pass-phrase
                        'csv': csv_data                       # csv data
                    })
print (rsp.status_code)
print (rsp.content)
print ('END POST csv data - JSON encoded')
print ('')
#

# POST items - JSON encoded
print ('*'*80)
print ('POST items - JSON encodeds')
print ('Num Items:' + str(len(items)))
rsp = requests.post('https://thither.direct/api/fms/post/',
                    json={
                        'fid': 'YourFlowID',              # Flow Metrics Statistics ID
                        'ps':  'optionalYourPassPhrase',  # API pass-phrase
                        'items': items,                   # list of items [[Metric ID, DateTime, Value],]
                    })
print (rsp.status_code)
print (rsp.content)
print ('END POST items - JSON encoded')
print ('')
#

