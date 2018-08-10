package direct.thither.lib.api.fms;
/*
 * Author Kashirin Alex (kashirin.alex@gmail.com)
 * THITHER.DIRECT
 * */


import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class FmsSetStatsQueue{
    private ConcurrentLinkedQueue<FmsSetStatsItem> q;
    private FmsClient clt;
    private int max;
    private int interval;
    private FmsSetStatsCallBack cb;
    private Semaphore q_c;
    private boolean run=true;

    public FmsSetStatsQueue(FmsClient client, int queue_max, int interval){
        q = new ConcurrentLinkedQueue<>();
        clt = client;
        this.interval = interval;
        max = queue_max>100000?100000:queue_max;
        q_c = new Semaphore(max, true);
        new Thread(new Runnable() {
            @Override
            public void run() {q_commit();}
        }).start();
    }
    public void set_callbacks(FmsSetStatsCallBack cb) {
        this.cb = cb;
    }

    public void add(FmsSetStatsItem stat){
        q.add(stat);
        q_c.release();
    }
    public int queued(){return q.size();}
    private void q_commit(){
        try { q_c.acquire(max); }catch (Exception e){}
        int q_sz;
        while (run){
            q_sz = q_c.availablePermits();
            try {
                if(interval>0)
                    q_c.tryAcquire(interval, q_sz>max?q_sz:max, TimeUnit.SECONDS);
                else
                    q_c.acquire(q_sz>max?q_sz:max);
            }catch (Exception e){}
            commit();
        }
        if(cb != null) cb.onQueueStop();
    }
    public void finalize_and_stop(){
        run = false;
        q_c.release(max);
    }
    public void commit() {
        StringBuilder csv_data = null;
        FmsRspSetStats r;
        int tries;
        int c = 0;
        while (!q.isEmpty()){
            if(csv_data == null) csv_data = new StringBuilder("mid,dt,v\n");

            c++;
            csv_data.append(q.poll().to_csv_line());

            if(c == max || (!run && q.isEmpty())){
                tries = 0;
                do{
                    r = clt.push_csv_data(csv_data.toString());

                    tries++;
                    if(r.code == 200) {
                        if (r.msg_code.equals("OK"))
                            break;
                        q.addAll(r.errors);
                        break;
                    }
                    else if(tries == 5 || r.code == 503 || r.code == 404) break;
                    else try { Thread.sleep(2000); } catch (Exception e){}
                }while(true);

                if(cb != null) {
                    try {
                        final FmsRspSetStats rsp = r;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {cb.onRspStats(rsp);}
                        }).start();
                    }catch(Exception e){}
                }

                c = 0;
                csv_data = null;
            }
        }
    }
    public interface FmsSetStatsCallBack {
        public abstract void onRspStats(FmsRspSetStats rsp);
        public abstract void onQueueStop();
    }
}