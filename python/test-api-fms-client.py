# -- coding: utf-8 --
# author Kashirin Alex

# # #  FLOW METRICS STATISTICS - API THITHER.DIRECT # # #

from libthither.api.fms import FlowMetricsStatisticsClient as FmsClient

import time
import datetime
import calendar


# Initiate FmsClient
client = FmsClient('YourFlowID',
                   pass_phrase='YourPassPhrase',
                   cipher='AES',
                   keep_alive=True)

#
print ('')
print ('*'*79)
print ('TEST EXPECTED OUTPUT - START')
print ('')


# GET METRICS INFO - START
print ('-'*79)
print ('Metrics Definitions: ')
print ('-'*20)
rsp = client.get_definitions('metrics')
if rsp.status_code != 200:
    print (rsp.status_code, rsp.content)
    exit()
metrics = rsp.json()['metrics']
for m_id in sorted(metrics.keys()):
    print ('Metric ID: '+m_id)
    for f, v in metrics[m_id].items():
        print ('  ' + f + ': ' + v)
# GET METRICS INFO - END

#

# CONFIGURATION
clock_screw_adj = 60  # ignore clock screw for test case
ts_begin = (2017, 1,  1,  0,  0,  0, 0, 0, 0)
ts_end = (2017,  12, 31, 23, 59, 59, 0, 0, 0)
from_ts = int(calendar.timegm(ts_begin))+clock_screw_adj
to_ts = int(calendar.timegm(ts_end))-clock_screw_adj

values = range(1, 11, 1)  # multiplier on hours passed plus it self
seconds_interval = 30
num_items = (1+int((to_ts-from_ts)/seconds_interval))*len(values)*len(metrics)

dt_begin = datetime.datetime.utcfromtimestamp(from_ts)
dt_end = datetime.datetime.utcfromtimestamp(to_ts)
num_days = (dt_end-dt_begin).days+1

commit_at = 40000*len(values)*len(metrics)
by_utc_ts = True

print ('-'*79)
print ('Populate stats:')
print ('-'*15)
print ('  From:            ' + datetime.datetime.fromtimestamp(from_ts).strftime('%Y-%m-%d %H:%M:%S'))
print ('  To:              ' + datetime.datetime.fromtimestamp(to_ts).strftime('%Y-%m-%d %H:%M:%S'))
print ('  UTC from:        ' + dt_begin.strftime('%Y-%m-%d %H:%M:%S'))
print ('  UTC to:          ' + dt_end.strftime('%Y-%m-%d %H:%M:%S'))
print ('  UTC ts from:     ' + str(from_ts))
print ('  UTC ts To:       ' + str(to_ts))
print ('  Number of Days:  ' + str(num_days))
print ('  Number of Items: ' + str(num_items))
print ('  Secs Interval:   ' + str(seconds_interval))
print ('  by UTC ts:       ' + str(by_utc_ts))
print ('  Push at N items: ' + str(commit_at))
print ('-'*40)
#


# POPULATE DATA - START
test_start = time.time()
items = []
metrics_total = {}
c = 0
item_ts = from_ts
while item_ts < to_ts:
    v_dif = int((item_ts-from_ts)/3600)
    for m_id in metrics.keys():
        if m_id not in metrics_total:
            metrics_total[m_id] = {'v': 0, 'c': 0}
        metric_total = metrics_total[m_id]

        for value in values:
            value *= v_dif+value
            if by_utc_ts:
                items.append([m_id, item_ts, value])
                # by_ts = False  # mix time form
            else:
                items.append([m_id, datetime.datetime.fromtimestamp(item_ts).strftime('%Y-%m-%d %H:%M:%S'), value])
                # by_ts = True
            metric_total['v'] += value
            metric_total['c'] += 1
            c += 1

    item_ts += seconds_interval
    # continue
    if c >= commit_at or item_ts >= to_ts:
        t_start = time.time()
        rsp = client.push_list(items)
        time_took = round(time.time()-t_start, 6)
        print (datetime.datetime.utcfromtimestamp(item_ts-seconds_interval).strftime('%Y-%m-%d %H:%M:%S'),
               time_took, c, rsp.status_code, rsp.content, rsp.json()['succeed'] == c)
        items = []
        c = 0
    #
print ('-' * 40)
print ('POPULATE DATA time took: ' + str(time.time()-test_start))
# POPULATE DATA - END
time.sleep(30)  # last commit minimal interval (10)

#

# CHECK TOTAL EXPECTED VALUES OF A METRIC - START
from_ts -= clock_screw_adj
to_ts -= clock_screw_adj
print ('-'*79)
print ('CHECK TOTAL EXPECTED VALUES OF A METRIC:')
print ('  From UTC:      ' + datetime.datetime.utcfromtimestamp(from_ts).strftime('%Y-%m-%d %H:%M:%S'))
print ('  To UTC:        ' + datetime.datetime.utcfromtimestamp(to_ts).strftime('%Y-%m-%d %H:%M:%S'))
print ('  From UTC ts:   '+str(from_ts))
print ('  To UTC ts:     '+str(to_ts))
print ('-'*40)
print ('')

for m_id in sorted(metrics.keys()):
    operation = metrics[m_id]['operation']
    page = 1
    total_value = 0
    items_count = 0
    ts_start = time.time()
    while True:
        rsp = client.get_stats(m_id,         # Your Metric ID
                               from_ts,      # from timestamp
                               to_ts,        # to timestamp
                               # base=metrics[m_id],  # time frame base - minutes, groups lower metric base to this base
                               # tz=timezone,         # adjust timezone UTC +/- minutes
                               time_format='%Y/%m/%d',  # default '%Y/%m/%d %H:%M' decreased with higher base
                               # limit=10000,  # results limit, 0:no-limit max:1,000,000
                               page=page     # start from page number
                               )
        if rsp.status_code != 200:
            print (page, total_value, rsp.status_code, rsp.content)
            exit()

        js_rsp = rsp.json()
        for date, value in js_rsp['items']:
            total_value += value
            items_count += 1

        if not js_rsp['next_page']:
            break
        page = js_rsp['next_page']
        #
    time_took = time.time()-ts_start

    expected_value = metrics_total[m_id]['v']
    if operation == 'avg':
        expected_value /= metrics_total[m_id]['c']
        total_value = total_value/items_count

    print ('METRIC ID ' + m_id + ' CHECK RESULT: '+('GOOD' if total_value == expected_value else 'BAD'))
    print ('  operation: ' + operation)
    print ('  count:     ' + str(items_count))
    print ('  value:     ' + str(total_value))
    print ('  expected:  ' + str(expected_value))
    print ('  time took: ' + str(time_took))
    # exit()
# CHECK TOTAL EXPECTED VALUES OF A METRIC - END

client.close()
print ('')
print ('TEST EXPECTED OUTPUT - END')
print ('*'*79)
print ('')
#


#
# python test-api-fms-client.py
#
# *******************************************************************************
# TEST EXPECTED OUTPUT - START
#
# -------------------------------------------------------------------------------
# Metrics Definitions:
# --------------------
# Metric ID: 2
#   tz: 0
#   name: E. Machine n.1233
#   section: 1-1-1-1-
#   timebase: 5
#   operation: sum
#   unit: 1
#   desc: Electricity Consumption Machine n.1233
# Metric ID: 3
#   tz: 0
#   name: E. Machine n.2221
#   section: 2-
#   timebase: 5
#   operation: sum
#   unit: 1
#   desc: Electricity consumption E. Machine n.2221
# Metric ID: 4
#   tz: 0
#   name: C. Machine n.1234
#   section: 1-1-1-1-
#   timebase: 5
#   operation: sum
#   unit: 2
#   desc: KG Capacity . Machine n.1234
# Metric ID: 5
#   tz: 0
#   name: E. Machine n.1234
#   section: 1-1-1-1-
#   timebase: 5
#   operation: avg
#   unit: 3
#   desc: Electricity Consumption Machine n.1234
# Metric ID: 6
#   tz: 0
#   name: E. Machine n.51234512
#   section: 1-1-1-1-
#   timebase: 1440
#   operation: sum
#   unit: 1
#   desc: E. Machine n.51234512
# -------------------------------------------------------------------------------
# Populate stats:
# ---------------
#   From:            2017-01-01 02:01:00
#   To:              2018-01-01 01:58:59
#   UTC from:        2017-01-01 00:01:00
#   UTC to:          2017-12-31 23:58:59
#   UTC ts from:     1483228860
#   UTC ts To:       1514764739
#   Number of Days:  365
#   Number of Items: 52559800
#   Secs Interval:   30
#   by UTC ts:       True
#   Push at N items: 2000000
# ----------------------------------------
# ('2017-01-14 21:21:00', 70.041, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-01-28 18:41:00', 64.014, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-02-11 16:01:00', 67.703, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-02-25 13:21:00', 65.017, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-03-11 10:41:00', 62.619, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-03-25 08:01:00', 66.023, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-04-08 05:21:00', 63.357, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-04-22 02:41:00', 63.593, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-05-06 00:01:00', 62.884, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-05-19 21:21:00', 65.125, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-06-02 18:41:00', 67.69, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-06-16 16:01:00', 64.45, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-06-30 13:21:00', 68.124, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-07-14 10:41:00', 68.452, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-07-28 08:01:00', 63.708, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-08-11 05:21:00', 65.562, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-08-25 02:41:00', 62.966, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-09-08 00:01:00', 63.612, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-09-21 21:21:00', 65.256, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-10-05 18:41:00', 66.762, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-10-19 16:01:00', 62.954, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-11-02 13:21:00', 63.436, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-11-16 10:41:00', 63.964, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-11-30 08:01:00', 67.309, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-12-14 05:21:00', 63.944, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-12-28 02:41:00', 68.005, 2000000, 200, '{"status": "OK", "succeed": 2000000}', True)
# ('2017-12-31 23:59:00', 17.763, 559800, 200, '{"status": "OK", "succeed": 559800}', True)
# ----------------------------------------
# POPULATE DATA time took: 1790.19099998
# -------------------------------------------------------------------------------
# CHECK TOTAL EXPECTED VALUES OF A METRIC:
#   From UTC:      2017-01-01 00:00:00
#   To UTC:        2017-12-31 23:57:59
#   From UTC ts:   1483228800
#   To UTC ts:     1514764679
# ----------------------------------------
#
# METRIC ID 2 CHECK RESULT: GOOD
#   operation: sum
#   count:     365
#   value:     253607955480
#   expected:  253607955480
#   time took: 1.59800004959
# METRIC ID 3 CHECK RESULT: GOOD
#   operation: sum
#   count:     365
#   value:     253607955480
#   expected:  253607955480
#   time took: 1.74000000954
# METRIC ID 4 CHECK RESULT: GOOD
#   operation: sum
#   count:     365
#   value:     253607955480
#   expected:  253607955480
#   time took: 1.80799984932
# METRIC ID 5 CHECK RESULT: GOOD
#   operation: avg
#   count:     365
#   value:     24125
#   expected:  24125
#   time took: 2.68899989128
# METRIC ID 6 CHECK RESULT: GOOD
#   operation: sum
#   count:     365
#   value:     253607955480
#   expected:  253607955480
#   time took: 0.138000011444
#
# TEST EXPECTED OUTPUT - END
# *******************************************************************************

