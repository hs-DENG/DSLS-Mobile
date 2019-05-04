package kr.ac.hansung.deng.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.ControlMode;
import dji.common.flightcontroller.simulator.SimulatorState;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
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
    private SendVirtualStickDataTask mSendVirtualStickDataTask;
    float mPitch=0;
    float mRoll=0;
    float mYaw=0;
    float mThrottle=0;

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
            flightController = aircraft.getFlightController();//TODO aircraft null일 경우 예외처리
            connect=true;
            Log.d("CustomDroneSDKManager","Controller Connect Success!");
            if(flightController!=null){
                flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null){
                            Log.d("setVirtualStickMode", "error" + djiError.getDescription().toString());
                        }else{
                            Log.d("setVirtualStickMode","Enable Virtual Stick Success");
                        }
                    }
                });
                flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
                flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
                flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
                flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
                flightController.getSimulator().setStateCallback(new SimulatorState.Callback() {
                    @Override
                    public void onUpdate(final SimulatorState stateData) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                String yaw = String.format("%.2f", stateData.getYaw());
                                String pitch = String.format("%.2f", stateData.getPitch());
                                String roll = String.format("%.2f", stateData.getRoll());
                                String positionX = String.format("%.2f", stateData.getPositionX());
                                String positionY = String.format("%.2f", stateData.getPositionY());
                                String positionZ = String.format("%.2f", stateData.getPositionZ());

                                Log.d("SimulatorsetCallback","Yaw : " + yaw + ", Pitch : " + pitch + ", Roll : " + roll + "\n" + ", PosX : " + positionX +
                                        ", PosY : " + positionY +
                                        ", PosZ : " + positionZ);
                            }
                        });
                    }
                });
            }
        }
    }
    // drone's function
    @Override
    public void getVideo(){
        initController();
        aircraft.getGimbal().rotate( Rotation.class.cast(RotationMode.ABSOLUTE_ANGLE), new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
        Log.d("CustomDroneSDKManager","turn left signal!");
    }

    @Override
    public void getCapture(){
        Log.d("CustomDroneSDKManager","turn left signal!");
    }

    // left joystick
    @Override
    public void leftJoystickAction(float xPosition, float yPosition){
        initController();
        if(Math.abs(xPosition) < 0.02 ){
            xPosition = 0;
        }

        if(Math.abs(yPosition) < 0.02 ){
            yPosition = 0;
        }
        xPosition /=1000;
        yPosition /=1000;
        float pitchJoyControlMaxSpeed = 10;
        float rollJoyControlMaxSpeed = 10;


        mPitch = (float)(pitchJoyControlMaxSpeed * xPosition);

        mRoll = (float)(rollJoyControlMaxSpeed * yPosition);
        Log.d("CustomDroneSDKManager", "mPitch " + mPitch + ", mRoll " + mRoll );
        if (null == mSendVirtualStickDataTimer) {
            mSendVirtualStickDataTask = new SendVirtualStickDataTask ();
            mSendVirtualStickDataTimer = new Timer();
            mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 100, 200);
        }
        Log.d("CustomDroneSDKManager","xPosition is : " + xPosition + ", yPosition is : " + yPosition);
        Log.d("CustomDroneSDKManager","left joystick action signal!");

    }

    @Override
    public void rightJoystickAction(float xPosition, float yPosition){
        initController();
        if(Math.abs(xPosition) < 0.02 ){
            xPosition = 0;
        }

        if(Math.abs(yPosition) < 0.02 ){
            yPosition = 0;
        }
        xPosition /=1000;
        yPosition /=1000;
        float verticalJoyControlMaxSpeed = 2;
        float yawJoyControlMaxSpeed = 30;

        mYaw = (float)(yawJoyControlMaxSpeed * xPosition);
        mThrottle = (float)(verticalJoyControlMaxSpeed * yPosition);
        Log.d("CustomDroneSDKManager", "mYaw " + mYaw + ", mThrottle " + mThrottle );
        if (null == mSendVirtualStickDataTimer) {
            mSendVirtualStickDataTask = new SendVirtualStickDataTask ();
            mSendVirtualStickDataTimer = new Timer();
            mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 0, 200);

        }
        Log.d("CustomDroneSDKManager","xPosition is : " + xPosition + ", yPosition is : " + yPosition);
        Log.d("CustomDroneSDKManager","right joystick action signal!");
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
                FlightControlData flightControlData = new FlightControlData( mPitch, mRoll, mYaw, mThrottle);
                //Log.d("SendDataTask","mPitch : " + mPitch + "mRoll : " + mRoll + "mYaw : " + mYaw + "mThrottle : " + mThrottle);
                flightController.sendVirtualStickFlightControlData(flightControlData, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            Log.d(TAG, djiError.getDescription());
                        } else {
                            Log.d(TAG, "Send JoyStick Data Successed");
                        }
                    }
                });
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

