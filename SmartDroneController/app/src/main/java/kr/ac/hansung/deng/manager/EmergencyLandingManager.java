package kr.ac.hansung.deng.manager;


import kr.ac.hansung.deng.manager.impl.DroneSDKManager;
import kr.ac.hansung.deng.service.DroneInfoService;
import kr.ac.hansung.deng.service.EmergencyService;

public class EmergencyLandingManager{

    private static EmergencyLandingManager instance = new EmergencyLandingManager();
    public static EmergencyLandingManager getInstance(){return instance;}
    private EmergencyLandingManager(){}

    private EmergencyService emergencyService;

    private DroneSDKManager sdkManager;

    public void init(DroneSDKManager sdkManager){
        this.sdkManager = sdkManager;


    }

    public void runService(){
    }
}
