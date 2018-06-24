# author Kashirin Alex

import sys
import csv
import base64

if sys.version_info.major == 3:
    from io import StringIO as NormStringIO
else:
    from io import BytesIO as NormStringIO

try:
    import requests
except:
    requests = None

try:
    from Cryptodome.Cipher import AES
except:
    AES = None

# get available compression lib
try:
    import zlib
    compressor = ('zlib', zlib.compress)
except:
    try:
        import brotli
        compressor = ('br', brotli.compress)
    except:
        compressor = None
#


class FlowMetricsStatisticsClient(object):
    __slots__ = ['u', 'fid', 'ps', 'cph', 'json', 's', 'ka', 'requests_args']

    root_url = '://thither.direct/api/fms/post/'
    errors = {
        0: 'param_empty',
        1: 'list_empty',
        2: 'csv_data_empty',
        3: 'bad_csv_header',
        4: 'bad_csv_row',
        5: 'flow_id_is_required',
    }
    dep_errs = {
        'AES': 'requested cipher AES, pkg Cryptodome is not installed!',
        'AES_ps': 'AES cipher require key(pass-phrase) 16, 24 or 32 in chars length!',
        'requests': 'pkg requests is not installed!'
    }

    def __init__(self, fid, **kwargs):
        """
            Initiate a new Client.

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
        """

        self.u = 'http'+('s' if kwargs.get('https', True) else '')+self.root_url

        self.fid = fid
        if not self.fid:
            raise Exception('error', self.errors[5])

        self.ps = kwargs.get('pass_phrase', '')

        self.cph = kwargs.get('cipher', '')
        if self.cph == 'AES':
            if AES is None:
                raise Exception('error', self.dep_errs['AES'])
            if len(self.ps) not in [16, 24, 32]:
                raise Exception('error', self.dep_errs['AES_ps'])

        if requests is None:
            raise Exception('error', self.dep_errs['requests'])
        self.s = requests.Session() if kwargs.get('keep_alive', False) else None

        self.json = kwargs.get('json', False)
        self.requests_args = kwargs.get('requests_args', None)
        #

    def set_params(self, params, first_item=None):
        """
            Set the corresponding parameters for a request.

            The function set the predefined parameters in the instance
            and if cipher is used calculates a nonce and a digest for authenticating

            Parameters
            ----------
            params : dict
                a reference to the dict to work with
            first_item : list
                a sample of the 1st item whether in csv/list or a single item
                in the follow ordered mid, dt, v
            Returns
            -------
            None
        """
        params['fid'] = self.fid  # Flow Metrics Statistics ID
        if not self.ps:
            return
        if self.cph == "AES":
            cipher = AES.new(b''+self.ps.encode("utf-8"), AES.MODE_EAX)
            cipher.encrypt((','.join(str(v) for v in first_item)).encode("utf-8"))
            params['digest'] = base64.b64encode(cipher.digest())
            params['nonce'] = base64.b64encode(cipher.nonce)
        else:
            params['ps'] = self.ps
        #

    @staticmethod
    def set_compression(params, data):
        params['comp'] = compressor[0]
        if compressor[0] in ['br', 'zlib']:
            return compressor[1](data.encode("utf-8"))
        return data
        #

    def push_single(self, mid, dt, v):
        """
            Push a single item to the server.

            Parameters
            ----------
            mid : str
                Your MetricID
            dt : str
                Date and time in format '%Y-%m-%d %H:%M:%S' unless otherwise specified on the metric configurations
            v : str/int
                value positive, negative or =equal

            Returns
            -------
            requests lib response or an rsp object with status_code and content {'status': 'bad_request', 'error': desc}
        """
        if not mid or not dt or (not v and v != 0):
            return self.bad_req(0)
        if not isinstance(v, int):
            try:
                tmp = int(v if v[0] != '=' else v[1:])
            except:
                return self.bad_req(0)

        params = {'mid': mid, 'dt': dt, 'v': v}
        self.set_params(params, [mid, dt, v])
        if self.json:
            return self.post(json=params)
        else:
            return self.post(headers={'Content-Type': 'application/x-www-form-urlencoded'}, params=params)
        #

    def push_list(self, items):
        """
            Push a list to the server.

            Extended description of function.

            Parameters
            ----------
            items : list
                list of items [[Metric ID, DateTime, Value],]
            Returns
            -------
            requests lib response or an rsp object with status_code and content {'status': 'bad_request', 'error': desc}
        """
        if not items:
            return self.bad_req(1)

        params = {}
        self.set_params(params, items[0])

        if self.json:
            params['items'] = items
            return self.post(json=params)
        else:
            # list converted to csv preferred for data-transfer
            csv_data = "\n".join(["mid,dt,v"] + [','.join([str(v) for v in item]) for item in items])
            if compressor is not None:
                csv_data = self.set_compression(params, csv_data)
            return self.post(params=params, files={'csv': csv_data})
        #

    def push_csv_data(self, csv_data):
        """
            Push a csv data to the server.

            Extended description of function.

            Parameters
            ----------
            csv_data : str
                a csv data with 'mid', 'dt', 'v' columns
            Returns
            -------
            requests lib response or an rsp object with status_code and content {'status': 'bad_request', 'error': desc}
        """
        if not csv_data:
            return self.bad_req(2)

        item = []
        if self.cph:  # get 1st item for auth-digest
            csv_file = NormStringIO(csv_data)
            dialect = csv.Sniffer().sniff(csv_file.read(2048))
            csv_file.seek(0)
            reader = csv.reader(csv_file, dialect)
            fc = {}
            h = False
            for row in reader:
                if not h:
                    h = True
                    for i, c in enumerate(row):
                        if c in ['mid', 'dt', 'v']:
                            fc[c] = i
                    if len(fc) != 3:
                        return self.bad_req(3)
                    continue
                if not row:
                    return self.bad_req(4)
                l_row = len(row)
                for f in ['mid', 'dt', 'v']:
                    if l_row <= fc[f]:
                        continue
                    item.append(row[fc[f]])
                break
            csv_file.close()
            if len(item) < 3:
                return self.bad_req(4)

        params = {}
        self.set_params(params, item)
        if self.json:
            params['csv'] = csv_data
            return self.post(json=params)
        else:
            if compressor is not None:
                csv_data = self.set_compression(params, csv_data)
            return self.post(params=params, files={'csv': csv_data})
        #

    @classmethod
    def bad_req(cls, c):
        rsp = object()
        rsp.status_code = c
        rsp.content = {'status': 'bad_request', 'error': cls.errors[c]}
        return rsp
        #

    def post(self, **kwargs):
        if self.requests_args is not None:
            kwargs.update(self.requests_args)
        if self.s is not None:
            return self.s.post(self.u, **kwargs)
        else:
            return requests.post(self.u, **kwargs)
        #

    def close(self):
        if self.s is not None:
            self.s.close()
        #





