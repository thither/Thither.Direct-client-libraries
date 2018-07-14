# -- coding: utf-8 --
# author Kashirin Alex

# # #  FLOW METRICS STATISTICS - API THITHER.DIRECT # # #

from libthither.api.fms import FlowMetricsStatisticsClient as FmsClient

import time
import datetime
dt_now = datetime.datetime.utcnow()


# Initiate FmsClient
client = FmsClient('YourFlowID',
                   pass_phrase='YourPassPhrase',
                   keep_alive=Truem,
                   # cipher='AES',
                   # version='v201807',
                   )

#
print ('')
print ('EXAMPLES FOR PUSHING DATA')
print ('')

#
print ('')
print ('*'*79)
print ('EXAMPLE FOR PUSHING SINGLE ITEM - START')

for c in range(1, 11):
    ts_start = time.time()
    rsp = client.push_single('2', client.utc_seconds(), c)
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
         for d in range(0, 1)              # days
         for c in range(1000, 3000, 1000)  # 2x values once at 0sec second at 30sec
         ]
print ('Num Items:' + str(len(items)))  # num_items=days*72000 () , value(5 minute time frame = 5x2x(1000+2000))

# CSV EXAMPLE DATA
csv_data = "\n".join(["mid,dt,v"]+[','.join([str(v) for v in item]) for item in items])
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

#
print ('')
print ('')
print ('EXAMPLES FOR GETTING DATA')
print ('')
print ('')

#
print ('*'*79)
print ('EXAMPLE FOR GETTING FLOW\'s units DEFINITIONS - START')

rsp = client.get_definitions('units')

if rsp.status_code == 200:
    print (rsp.json()['units'])
else:
    print (rsp.status_code)
    print (rsp.content)
print ('EXAMPLE FOR GETTING FLOW\'s units DEFINITIONS - END')
print ('*'*79)

#
print ('')
print ('*'*79)
print ('EXAMPLE FOR GETTING FLOW\'s sections DEFINITIONS - START')

rsp = client.get_definitions('sections',
                             section=''  # sections only on section-id level
                             )
if rsp.status_code == 200:
    print (rsp.json()['sections'])
else:
    print (rsp.status_code)
    print (rsp.content)
print ('EXAMPLE FOR GETTING FLOW\'s sections DEFINITIONS - END')
print ('*'*79)

#
print ('')
print ('*'*79)
print ('EXAMPLE FOR GETTING FLOW\'s metrics DEFINITIONS - START')

rsp = client.get_definitions('metrics',
                             section='2-',       # only metrics on section-id level
                             unit='1',           # only metrics with unit-id
                             operation='',       # only metrics with time base join operation of avg/sum
                             timebase='',        # only metrics with timebase(minutes)
                             tz=''               # only metrics with GMT timezone(-/+ hours)
                             )
if rsp.status_code == 200:
    print (rsp.json()['metrics'])
else:
    print (rsp.status_code)
    print (rsp.content)
print ('EXAMPLE FOR GETTING FLOW\'s metrics DEFINITIONS - END')
print ('*'*79)

#
print ('')
print ('*'*79)
print ('EXAMPLE FOR GETTING all FLOW\'s DEFINITIONS - START')

rsp = client.get_definitions()
if rsp.status_code == 200:
    js_rsp = rsp.json()
    for typ in ['units', 'sections', 'metrics']:
        print (js_rsp[typ])
else:
    print (rsp.status_code)
    print (rsp.content)
print ('EXAMPLE FOR GETTING all FLOW\'s DEFINITIONS - END')
print ('*'*79)

#
print ('')
print ('*'*79)
print ('EXAMPLE FOR GETTING STATS DATA - START')
rsp = client.get_definitions('metrics')
if rsp.status_code == 200:
    metrics = rsp.json()['metrics']
else:
    print (rsp.status_code)
    print (rsp.content)
    exit()

# last N days

timezone = int(-1*(time.timezone/60)+time.daylight*60)
# timezone = (dt_now-datetime.datetime.utcnow()).seconds/60

dt_begin = dt_now - datetime.timedelta(days=8)
dt_begin = datetime.datetime(dt_begin.year, dt_begin.month, dt_begin.day, 0, 0, 0)
from_ts = int(time.mktime(dt_begin.timetuple()))

# dt_end = dt_now - datetime.timedelta(days=1)
# dt_end = datetime.datetime(dt_end.year, dt_end.month, dt_end.day, 23, 59, 59)
# to_ts = int(time.mktime(dt_end.timetuple()))+1
to_ts = from_ts+7*24*60*60-1

print ('now: '+dt_now.strftime('%Y-%m-%d %H:%M:%S'))
print ('from_ts: '+str(from_ts))
print ('from: '+datetime.datetime.fromtimestamp(from_ts).strftime('%Y-%m-%d %H:%M:%S'))
print ('to: '+datetime.datetime.fromtimestamp(to_ts).strftime('%Y-%m-%d %H:%M:%S'))
print ('secs interval: '+str(to_ts - from_ts))
print ('tz: '+str(timezone))

# get daily stats for the last week for the available metric IDs
for metric_id in list(metrics.keys()):
    page = 1
    while True:
        rsp = client.get_stats(metric_id,    # Your Metric ID
                               from_ts,      # from timestamp
                               to_ts,        # to timestamp
                               base=5,      # time frame base - minutes, groups lower metric base to this base
                               tz=timezone,  # timezone GMT +/- minutes
                               time_format='%Y/%m/%d %H:%M',  # default '%Y/%m/%d %H:%M' decreased with higher base
                               limit=1000,   # results limit, 0:no-limit max:1,000,000
                               page=page     # start from page number
                               )

        if rsp.status_code == 200:
            js_rsp = rsp.json()
            # WORK WITH STATS DATA
            print (page, len(js_rsp['items']))
            if js_rsp['items']:
                print (js_rsp['items'][0], js_rsp['items'][-1])

            if js_rsp['next_page'] == 0:
                break
            page = js_rsp['next_page']
        else:
            print (rsp.status_code)
            print (rsp.content)
            break
    print ('*'*24)

print ('EXAMPLE FOR GETTING STATS DATA - END')
print ('*'*79)

client.close()
