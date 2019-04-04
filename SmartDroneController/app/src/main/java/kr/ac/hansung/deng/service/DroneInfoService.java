package kr.ac.hansung.deng.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import kr.ac.hansung.deng.manager.DroneSDKManager;

public class DroneInfoService extends Service {
    private DroneSDKManager sdkManager;

    public DroneInfoService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
