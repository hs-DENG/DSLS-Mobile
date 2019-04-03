package kr.ac.hansung.deng.manager;


import kr.ac.hansung.deng.service.EmergencyService;

public class EmergencyLandingManager{

    private static EmergencyLandingManager instance = new EmergencyLandingManager();
    public static EmergencyLandingManager getInstance(){return instance;}
    private EmergencyLandingManager(){}


    private EmergencyService service = new EmergencyService();

    public DroneSDKManager sdkManager;


    public DroneSDKManager getSdkManager() {
        return sdkManager;
    }
    public void setSdkManager(DroneSDKManager sdkManager){
        this.sdkManager = sdkManager;
    }

}
