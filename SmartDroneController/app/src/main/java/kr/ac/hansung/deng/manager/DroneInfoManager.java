package kr.ac.hansung.deng.manager;

import kr.ac.hansung.deng.manager.impl.DroneSDKManager;
import kr.ac.hansung.deng.service.DroneInfoService;

public class DroneInfoManager {
    private static DroneInfoManager instance = new DroneInfoManager();
    public static DroneInfoManager getInstance(){return instance;}
    private DroneInfoManager(){}

    private DroneInfoService droneInfoService;
    private DroneSDKManager sdkManager;

    public void init(DroneSDKManager sdkManager){
        this.sdkManager = sdkManager;


    }
    public void runService(){
    }
}
