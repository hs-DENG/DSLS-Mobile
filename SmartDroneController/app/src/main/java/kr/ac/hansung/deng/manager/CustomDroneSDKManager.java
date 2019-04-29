package kr.ac.hansung.deng.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.util.CommonCallbacks;
import dji.log.DJILog;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import kr.ac.hansung.deng.sdk.DJISimulatorApplication;

public class CustomDroneSDKManager implements SDKManager{
    // TAG
    private static final String TAG = "SDKManager";

    private Context mContext;
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);

    // singleton
    private static CustomDroneSDKManager instance = new CustomDroneSDKManager();
    private CustomDroneSDKManager(){}
    public static CustomDroneSDKManager getInstance(){return instance;}

    // Aircraft Controller Reference
    private FlightController flightController;
    private Aircraft aircraft;
    private boolean connect=false;

    // joystick reference
    private Timer mSendVirtualStickDataTimer;
    private TimerTask mSendVirtualStickDataTask;
    float mPitch;
    float mRoll;
    float mYaw;
    float mThrottle;

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
        if(!connect) {
            aircraft = DJISimulatorApplication.getAircraftInstance();
            flightController = aircraft.getFlightController();
            connect=true;
            Log.d(TAG,"Controller Connect Success!");
            if(flightController!=null){
                flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null){
                            Log.d(TAG,djiError.getDescription().toString());
                        }else{
                            Log.d(TAG,"Enable Virtual Stick Success");
                        }
                    }
                });
            }
        }
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
    public void turnLeft(float xPosition, float yPosition){
        initController();
        if(Math.abs(xPosition) < 0.02 ){
            xPosition = 0;
        }

        if(Math.abs(yPosition) < 0.02 ){
            yPosition = 0;
        }
        float pitchJoyControlMaxSpeed = 10;
        float rollJoyControlMaxSpeed = 10;


        mPitch = (float)(pitchJoyControlMaxSpeed * xPosition);

        mRoll = (float)(rollJoyControlMaxSpeed * yPosition);

        if (null == mSendVirtualStickDataTimer) {
            mSendVirtualStickDataTask = new SendVirtualStickDataTask ();
            mSendVirtualStickDataTimer = new Timer();
            mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 100, 200);
        }
        mPitch=0;
        mRoll=0;
        mYaw=0;
        mThrottle=0;
        Log.d("CustomDroneSDKManager","turn left signal!");

    }
    @Override
    public void turnRight(float xPosition, float yPosition){
        initController();
        if(Math.abs(xPosition) < 0.02 ){
            xPosition = 0;
        }

        if(Math.abs(yPosition) < 0.02 ){
            yPosition = 0;
        }
        float pitchJoyControlMaxSpeed = 10;
        float rollJoyControlMaxSpeed = 10;


        mPitch = (float)(pitchJoyControlMaxSpeed * xPosition);

        mRoll = (float)(rollJoyControlMaxSpeed * yPosition);

        if (null == mSendVirtualStickDataTimer) {
            mSendVirtualStickDataTask = new SendVirtualStickDataTask ();
            mSendVirtualStickDataTimer = new Timer();
            mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 100, 200);

        }
        Log.d("CustomDroneSDKManager","turn right signal!");
    }
    @Override
    public void up(float xPosition, float yPosition){
        Log.d("CustomDroneSDKManager","up signal!");
    }
    @Override
    public void down(float xPosition, float yPosition){
        Log.d("CustomDroneSDKManager","down signal!");
    }

    //right joystick
    @Override
    public void right(float xPosition, float yPosition){
        Log.d("CustomDroneSDKManager","right signal!");
    }
    @Override
    public void left(float xPosition, float yPosition){
        Log.d("CustomDroneSDKManager","left signal!");
    }
    @Override
    public void forward(float xPosition, float yPosition){
        Log.d("CustomDroneSDKManager","forward signal!");
    }
    @Override
    public void back(float xPosition, float yPosition){
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
    class SendVirtualStickDataTask extends TimerTask {
        @Override
        public void run() {

            if (flightController != null) {
                flightController.sendVirtualStickFlightControlData(
                        new FlightControlData(
                                mPitch, mRoll, mYaw, mThrottle
                        ), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {

                            }
                        }
                );
            }
        }
    }

    public boolean isConnect() {
        return connect;
    }

    public void setConnect(boolean connect) {
        this.connect = connect;
    }
}

