package kr.ac.hansung.deng.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import kr.ac.hansung.deng.manager.EmergencyLandingManager;

public class EmergencyService extends Service {

    private static final String TAG = EmergencyService.class.getSimpleName();
    private Thread mThread = null;
    private int mCount = 0;

    public EmergencyService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        // 스레드
        if(mThread == null){
            mThread = new Thread("My Thread"){
                @Override
                public void run() {

                    for(int i=0; i<100; i++){
                        mCount++;
                        try{
                            Thread.sleep(1000);
                        }catch(Exception e){}

                        if(mThread != Thread.currentThread()) break;

                        Log.d(TAG, "서비스 동작중 : " + mCount);
                    }
                }
            };
            mThread.start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy()");

        if(mThread != null)
            mThread = null;

    }


    public class MyBinder extends Binder {
        public EmergencyService getService(){
            return EmergencyService.this;
        }
    }

    private IBinder mBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    public int getCount(){
        return mCount;
    }
}