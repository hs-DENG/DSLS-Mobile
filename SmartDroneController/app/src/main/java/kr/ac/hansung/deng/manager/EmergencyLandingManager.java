package kr.ac.hansung.deng.manager;


public class EmergencyLandingManager {
    private static EmergencyLandingManager instance = new EmergencyLandingManager();
    public static EmergencyLandingManager getInstance(){return instance;}
    private EmergencyLandingManager(){}

    public DroneSDKManager sdkManager;
    private SagementManager sagementManager;


    public DroneSDKManager getSdkManager() {
        return sdkManager;
    }
    public void setSdkManager(DroneSDKManager sdkManager){
        this.sdkManager = sdkManager;
    }
}
