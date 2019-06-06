package kr.ac.hansung.deng.manager;

public class DroneInfoManager {
    private static DroneInfoManager instance = new DroneInfoManager();
    public static DroneInfoManager getInstance(){return instance;}
    private DroneInfoManager(){}

    private SDKManager sdkManager;

    public void init(SDKManager sdkManager){
        this.sdkManager = sdkManager;


    }
    public void runService(){
    }
}
