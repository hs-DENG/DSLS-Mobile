package kr.ac.hansung.deng.manager.impl;

import android.os.Parcel;
import android.os.Parcelable;

import kr.ac.hansung.deng.manager.SDKManager;


public class DroneSDKManager implements SDKManager, Parcelable {
    public DroneSDKManager(){}

    public DroneSDKManager(Parcel parcel){
        // field mapping

    }

    public static final Parcelable.Creator<DroneSDKManager> CREATOR = new Parcelable.Creator<DroneSDKManager>() {
        @Override
        public DroneSDKManager createFromParcel(Parcel parcel) {
            return new DroneSDKManager(parcel);
        }
        @Override
        public DroneSDKManager[] newArray(int size) {
            return new DroneSDKManager[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }


    @Override
    public void connect() {

    }

    @Override
    public void getVideo() {

    }

    @Override
    public void getCapture() {

    }

    @Override
    public void turnLeft() {

    }

    @Override
    public void turnRight() {

    }

    @Override
    public void up() {

    }

    @Override
    public void down() {

    }

    @Override
    public void right() {

    }

    @Override
    public void left() {

    }

    @Override
    public void forward() {

    }

    @Override
    public void back() {

    }

    @Override
    public void takeOffDrone() {

    }

    @Override
    public void landingDrone() {

    }
}
