package kr.ac.hansung.deng.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.util.CommonCallbacks;
import dji.log.DJILog;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import kr.ac.hansung.deng.manager.impl.DroneSDKManager;
import kr.ac.hansung.deng.sdk.DJISimulatorApplication;

public class CustomDroneSDKManager extends DroneSDKManager{

    private static CustomDroneSDKManager instance = new CustomDroneSDKManager();
    private Context mContext;
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static final String TAG = "SDKManager";
    private FlightController flightController;
    private Aircraft aircraft;
    private CustomDroneSDKManager(){}
    public static CustomDroneSDKManager getInstance(){return instance;}

    // connection
    @Override
    public void connect(){
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG,"registering, pls wait...");
                    DJISDKManager.getInstance().registerApp(mContext, new DJISDKManager.SDKManagerCallback() {
                        @Override
                        public void onRegister(DJIError djiError) {
                            if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                                DJILog.e("App registration", DJISDKError.REGISTRATION_SUCCESS.getDescription());
                                DJISDKManager.getInstance().startConnectionToProduct();
                                Log.d(TAG,"Register Success ca");
                            } else {
                                Log.d(TAG, "Register sdk fails, check network is available");
                            }
                            Log.v(TAG, djiError.getDescription());
                        }

                        @Override
                        public void onProductDisconnect() {
                            Log.d(TAG, "onProductDisconnect");
                            Log.d(TAG,"Product Disconnected");

                        }
                        @Override
                        public void onProductConnect(BaseProduct baseProduct) {
                            Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
                            Log.d(TAG,"Product Connected");

                        }
                        @Override
                        public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                                      BaseComponent newComponent) {

                            if (newComponent != null) {
                                newComponent.setComponentListener(new BaseComponent.ComponentListener() {

                                    @Override
                                    public void onConnectivityChange(boolean isConnected) {
                                        Log.d(TAG, "onComponentConnectivityChanged: " + isConnected);
                                    }
                                });
                            }
                            Log.d(TAG,
                                    String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                                            componentKey,
                                            oldComponent,
                                            newComponent));
                        }
                    });
                }
            });
        }
        Log.d("CustomDroneSDKManager","connect signal!");
        initController();

    }

    public void initController(){
        aircraft = DJISimulatorApplication.getAircraftInstance();
        flightController = aircraft.getFlightController();
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
        initController();
        Log.d("CustomDroneSDKManager","take-Off signal!");
        if (flightController != null){
            flightController.startTakeoff(
                    new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null) {
                                Log.d(TAG,djiError.getDescription());
                            } else {
                                Log.d(TAG,"Take off Success");
                            }
                        }
                    }
            );
        }
    }
    @Override
    public void landing() {
        initController();
        if (flightController != null) {
            flightController.startLanding(
                    new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null) {
                                Log.d(TAG, djiError.getDescription());
                            } else {
                                Log.d(TAG, "Start Landing");
                            }
                        }
                    }
            );
            Log.d("CustomDroneSDKManager", "landing signal!");
        }
    }

    public Context getmContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }
}
