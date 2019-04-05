package kr.ac.hansung.deng.app;

import android.os.Parcel;
import android.util.Log;
import android.widget.Toast;

import kr.ac.hansung.deng.manager.impl.DroneSDKManager;

public class CustomDroneSDKManager extends DroneSDKManager {

    public CustomDroneSDKManager(){}


    public CustomDroneSDKManager(Parcel parcel) {
        super(parcel);
    }

    // connection
    @Override
    public void connect(){
        Log.d("CustomDroneSDKManager","turn left signal!");
    }

    // drone's function
    @Override
    public void getVideo(){
        Log.d("CustomDroneSDKManager","turn left signal!");
    }

    @Override
    public void getCapture(){
        Log.d("CustomDroneSDKManager","turn left signal!");
    }

    // left joystick
    @Override
    public void turnLeft(){
        Log.d("CustomDroneSDKManager","turn left signal!");
    }
    @Override
    public void turnRight(){
        Log.d("CustomDroneSDKManager","turn right signal!");
    }
    @Override
    public void up(){
        Log.d("CustomDroneSDKManager","up signal!");
    }
    @Override
    public void down(){
        Log.d("CustomDroneSDKManager","down signal!");
    }

    //right joystick
    @Override
    public void right(){
        Log.d("CustomDroneSDKManager","right signal!");
    }
    @Override
    public void left(){
        Log.d("CustomDroneSDKManager","left signal!");
    }
    @Override
    public void forward(){
        Log.d("CustomDroneSDKManager","forward signal!");
    }
    @Override
    public void back(){
        Log.d("CustomDroneSDKManager","back signal!");
    }
    @Override
    public void takeOff(){
        Log.d("CustomDroneSDKManager","take-Off signal!");
    }
    @Override
    public void landing(){
        Log.d("CustomDroneSDKManager","landing signal!");
    }

}
