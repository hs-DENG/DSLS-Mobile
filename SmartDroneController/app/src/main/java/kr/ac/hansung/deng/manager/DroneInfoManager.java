package kr.ac.hansung.deng.manager;

public class DroneInfoManager {
    private static DroneInfoManager instance = new DroneInfoManager();
    public static DroneInfoManager getInstance(){return instance;}
    private DroneInfoManager(){}

   // private DroneInfoService droneInfoService;
    private CustomDroneSDKManager sdkManager;

    public void init(CustomDroneSDKManager sdkManager){
        this.sdkManager = sdkManager;


    }
    public void runService(){
    }
}
