package kr.ac.hansung.deng.app;

import android.os.Parcel;

import kr.ac.hansung.deng.manager.impl.DroneSDKManager;

public class CustomerDroneSDKManager extends DroneSDKManager {
    public CustomerDroneSDKManager(){}


    public CustomerDroneSDKManager(Parcel parcel) {
        super(parcel);
    }

    // connection
    @Override
    public void connect(){

    }

    // drone's function
    @Override
    public void getVideo(){

    }

    @Override
    public void getCapture(){

    }

    // left joystick
    @Override
    public void turnLeft(){
    }
    @Override
    public void turnRight(){
    }
    @Override
    public void up(){
    }
    @Override
    public void down(){
    }

    //right joystick
    @Override
    public void right(){
    }
    @Override
    public void left(){
    }
    @Override
    public void forward(){
    }
    @Override
    public void back(){
    }
    @Override
    public void takeOffDrone(){
    }
    @Override
    public void landingDrone(){
    }

}
