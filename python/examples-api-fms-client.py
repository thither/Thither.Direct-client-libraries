# -- coding: utf-8 --

# # #  FLOW METRICS STATISTICS - API THITHER.DIRECT # # #

from libthither.api.fms import FlowMetricsStatisticsClient as FmsClient

import time
import datetime
dt_now = datetime.datetime.now()


# Initiate FmsClient
client = FmsClient('1F',
                   pass_phrase='1234567812345678',
                   cipher='AES',
                   keep_alive=True)


print ('')
print ('*'*79)
print ('EXAMPLE FOR PUSHING SINGLE ITEM - START')

for c in range(1, 11):
    ts_start = time.time()
    rsp = client.push_single('2', dt_now.strftime('%Y-%m-%d %H:%M:%S'), c)
    print (c, time.time()-ts_start)
    print (rsp.status_code)
    print (rsp.content)

print ('EXAMPLE FOR PUSHING SINGLE ITEM - END')
print ('*'*79)

#

print ('')
print ('AUTO-GENERATED EXAMPLE ITEMS DATA - START')
items = [[mid, (dt_now - datetime.timedelta(days=d, minutes=m, seconds=s)).strftime('%Y-%m-%d %H:%M:%S'), c]
         for mid in range(2, 7)            # 5 metric IDs range
         for s in range(0, 60, 30)         # seconds in minute / twice a minute
         for m in range(0, 3600)           # minutes in 1 day
         for d in range(0, 28)             # 28 days
         for c in range(1000, 3000, 1000)  # 2x values once at 0sec second at 30sec
         ]
print ('Num Items:' + str(len(items)))  # 504000 (items) , value(5 minute time frame = 5x2x(1000+3000))

# CSV EXAMPLE DATA
csv_data = "mid,dt,v\n"
for item in items:
    csv_data += ','.join([str(v) for v in item])+"\n"
print ('csv_data size: ' + str(len(csv_data)))
print ('AUTO-GENERATED EXAMPLE ITEMS DATA - END')


#
print ('')
print ('*'*79)
print ('EXAMPLE FOR PUSHING MULTIPLE ITEM - push_list - START')

# push list of items [[Metric ID, DateTime, Value],]
rsp = client.push_list(items)
print (rsp.status_code)
print (rsp.content)

print ('EXAMPLE FOR PUSHING MULTIPLE ITEM - push_list - END')
print ('*'*79)

#
print ('')
print ('*'*79)
print ('EXAMPLE FOR PUSHING MULTIPLE ITEM - push_csv_data - START')

rsp = client.push_csv_data(csv_data)
print (rsp.status_code)
print (rsp.content)

print ('EXAMPLE FOR PUSHING MULTIPLE ITEM - push_csv_data - END')
print ('*'*79)

client.close()
rsp = client.push_single('2', dt_now.strftime('%Y-%m-%d %H:%M:%S'), 123)
exit()





