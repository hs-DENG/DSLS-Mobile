package kr.ac.hansung.deng.manager;


import android.content.Intent;

import kr.ac.hansung.deng.app.MainActivity;
import kr.ac.hansung.deng.service.EmergencyService;

public class EmergencyLandingManager{

    private static EmergencyLandingManager instance = new EmergencyLandingManager();
    public static EmergencyLandingManager getInstance(){return instance;}
    private EmergencyLandingManager(){}

    private EmergencyService emergencyService;

    private CustomDroneSDKManager sdkManager;

    private MainActivity mainActivity;

    public void init(CustomDroneSDKManager sdkManager){
        this.sdkManager = sdkManager;


    }

    public void runService(){
        emergencyService = new EmergencyService();
        emergencyService.setActivity(mainActivity);
        // 인자값 내맘대로 넣었음 의미없는 매개변수임... 태성
        emergencyService.setBitmap1(sdkManager.getCaptureView());
        emergencyService.onStartCommand(new Intent() ,0 , 0);
    }

    public void setActivity(MainActivity mainActivity){

        this.mainActivity = mainActivity;
    }

    public EmergencyService getEmergencyService() {
        return emergencyService;
    }

    public void setEmergencyService(EmergencyService emergencyService) {
        this.emergencyService = emergencyService;
    }
}
