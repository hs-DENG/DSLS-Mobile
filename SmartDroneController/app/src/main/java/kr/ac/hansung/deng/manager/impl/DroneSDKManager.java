package kr.ac.hansung.deng.manager.impl;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;

import kr.ac.hansung.deng.manager.SDKManager;


public class DroneSDKManager implements SDKManager{
    public DroneSDKManager(){}

    @Override
    public void connect() {
        Log.d("DroneSDKManager","original connect");
    }

    @Override
    public void getVideo() {
        Log.d("DroneSDKManager","original getVideo");
    }

    @Override
    public void getCapture() {
        Log.d("DroneSDKManager","original getCapture");
    }

    @Override
    public void turnLeft() {
        Log.d("DroneSDKManager","original turnLeft");
    }

    @Override
    public void turnRight() {
        Log.d("DroneSDKManager","original turnRight");
    }

    @Override
    public void up() {
        Log.d("DroneSDKManager","original up");
    }

    @Override
    public void down() {
        Log.d("DroneSDKManager","original down");
    }

    @Override
    public void right() {
        Log.d("DroneSDKManager","original right");
    }

    @Override
    public void left() {
        Log.d("DroneSDKManager","original left");
    }

    @Override
    public void forward() {
        Log.d("DroneSDKManager","original forward");
    }

    @Override
    public void back() {
        Log.d("DroneSDKManager","original back");
    }

    @Override
    public void takeOff() {
        Log.d("DroneSDKManager","original takeOff");
    }

    @Override
    public void landing() {
        Log.d("DroneSDKManager","original landing");
    }

}
