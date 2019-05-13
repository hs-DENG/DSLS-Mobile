package kr.ac.hansung.deng.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.TextureView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.camera.SystemState;
import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.ControlMode;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.simulator.SimulatorState;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.product.Model;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.util.CommonCallbacks;
import dji.log.DJILog;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import kr.ac.hansung.deng.sdk.DJISimulatorApplication;
import kr.ac.hansung.deng.sdk.FPVApplication;

public class CustomDroneSDKManager implements SDKManager, TextureView.SurfaceTextureListener {
    // TAG
    private static final String TAG = "CustomDroneSDKManager";

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
    private Timer sendVirtualStickDataTimer;
    private SendVirtualStickDataTask sendVirtualStickDataTask;
    float mPitch=0; // �յ�
    float mRoll=0; // �¿�
    float mYaw=0; // ȸ��
    float mThrottle=0; // ����

    // Codec for video live view
    protected DJICodecManager mCodecManager = null;
    protected VideoFeeder.VideoDataListener mReceivedVideoDataListener = null;
    //protected Camera camera;
    private TextureView myVideoSurface = null;
    private Handler handler;
    private Thread mThread = null;
    private boolean initFlag = false;

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
        Log.d(TAG,"connect signal!");
        initController();
    }

    public void initController(){
        if(aircraft == null || flightController == null) {
            aircraft = DJISimulatorApplication.getAircraftInstance();
            flightController = aircraft.getFlightController();//TODO aircraft null�� �� ó�� �������
            //       connect=true;
            Log.d(TAG, "Controller Connect Success!");
            if (flightController != null) {

                flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            Log.d(TAG, "error" + djiError.getDescription().toString());
                            initController();
                        } else {
                            Log.d(TAG, "Enable Virtual Stick Success");
                        }
                    }
                });
                flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
                flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
                flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
                flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
            }
        }
        //   }
    }
    // drone's function
    @Override
    public void getVideo(TextureView textureView){
        Log.d(TAG,"get video signal!");
        myVideoSurface = textureView;
        mReceivedVideoDataListener = new VideoFeeder.VideoDataListener() {

            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };



        if(mThread == null){
            mThread = new Thread("My Thread"){
                @Override
                public void run(){

                    while(!initFlag){
                        //Log.d(TAG, "myVideoSurface is " + myVideoSurface);
                        if(myVideoSurface != null) {
                            //Log.d(TAG, "1");
                            // The callback for receiving the raw H264 video data for camera live view
                            if(!initFlag){
                                Log.d(TAG, "mVideoSurface : " + myVideoSurface);
                                initPreviewer();
                                initFlag = true;
                            }

                            Camera camera = FPVApplication.getCameraInstance();

                            if (camera != null) {

                                camera.setSystemStateCallback(new SystemState.Callback() {
                                    @Override
                                    public void onUpdate(SystemState cameraSystemState) {
                                        if (null != cameraSystemState) {

                                            int recordTime = cameraSystemState.getCurrentVideoRecordingTimeInSeconds();
                                            int minutes = (recordTime % 3600) / 60;
                                            int seconds = recordTime % 60;

                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            };
            mThread.start();
        }
    }
    private void initPreviewer(){
        Log.d(TAG, "initPreviewer()");

        BaseProduct product = FPVApplication.getProductInstance();

        if(product == null || !product.isConnected()){
            //showToast(getString(R.string.disconnected));
            Log.d(TAG, "if");
        }else {
            Log.d(TAG, "else");
            if(myVideoSurface != null){
                myVideoSurface.setSurfaceTextureListener(this);
            }
            if(!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)){
                Log.d(TAG, "else");
                VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(mReceivedVideoDataListener);
            }
        }
    }
    @Override
    public void removeVideo(){
        Log.d(TAG,"removeVideo() signal!");
        Camera camera = FPVApplication.getCameraInstance();
        if(camera != null){
            //Reset the callback
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(null);
        }
    }

    @Override
    public void getCapture(TextureView textureView){
        Log.d(TAG,"getCapture() signal!");

        textureView.buildDrawingCache();
        Bitmap captureView = textureView.getBitmap(textureView.getWidth(),textureView.getHeight());
        //captureView.setHasAlpha(true);
        FileOutputStream fos;

        String strFolderPath = Environment.getExternalStorageDirectory() + "/Pictures/SDC";

        File myFile = new File(strFolderPath);

        if(!myFile.exists()) {
            myFile.mkdirs();
        }

        String strFilePath = strFolderPath + "/" + System.currentTimeMillis() + ".png";
        File fileCacheItem = new File(strFilePath);

        try {
            fos = new FileOutputStream(fileCacheItem);
            captureView.compress(Bitmap.CompressFormat.PNG, 100, fos);

            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://"+ strFilePath)));
            Log.d(TAG,"capture success");
            Log.d(TAG, strFilePath);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    // left joystick

    @Override
    public void up() {
        initController();
        mPitch=0;
        mRoll=0;
        mYaw = (float)0;
        mThrottle = (float)0.3;
//        if (null == sendVirtualStickDataTimer) {
//            sendVirtualStickDataTask = new SendVirtualStickDataTask();
//            sendVirtualStickDataTimer = new Timer();
//            sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 100, 200);
//        }
        flightController.sendVirtualStickFlightControlData(new FlightControlData(mPitch, mRoll, mYaw, mThrottle), new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if(djiError != null){
                    Log.d(TAG,"send data error is : " + djiError.getDescription().toString());
                }
                else {
                    Log.d(TAG,"send successed");
                }
            }});
        Log.d(TAG,"up signal");
    }

    @Override
    public void down() {
        initController();
        mPitch=0;
        mRoll=0;
        mYaw = (float)0;
        mThrottle = -(float)0.3;
//        if (null == sendVirtualStickDataTimer) {
//            sendVirtualStickDataTask = new SendVirtualStickDataTask();
//            sendVirtualStickDataTimer = new Timer();
//            sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 100, 200);
//        }
        flightController.sendVirtualStickFlightControlData(new FlightControlData(mPitch, mRoll, mYaw, mThrottle), new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if(djiError != null){
                    Log.d(TAG,"send data error is : " + djiError.getDescription().toString());
                }
                else {
                    Log.d(TAG,"send successed");
                }
            }});
        Log.d(TAG,"down signal");
    }

    @Override
    public void turnLeft() {
        initController();
        mPitch=0;
        mRoll=0;
        mYaw = -(float)0.2;
        mThrottle = (float)0;
//        if (null == sendVirtualStickDataTimer) {
//            sendVirtualStickDataTask = new SendVirtualStickDataTask();
//            sendVirtualStickDataTimer = new Timer();
//            sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 100, 200);
//        }
        flightController.sendVirtualStickFlightControlData(new FlightControlData(mPitch, mRoll, mYaw, mThrottle), new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if(djiError != null){
                    Log.d(TAG,"send data error is : " + djiError.getDescription().toString());
                }
                else {
                    //Log.d(TAG,"send successed");
                }
            }});
        Log.d(TAG,"turn left signal");
    }

    @Override
    public void turnRight() {
        initController();
        mPitch=0;
        mRoll=0;
        mYaw = (float)0.2;
        mThrottle = (float)0;
//        if (null == sendVirtualStickDataTimer) {
//            sendVirtualStickDataTask = new SendVirtualStickDataTask();
//            sendVirtualStickDataTimer = new Timer();
//            sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 100, 200);
//        }
        flightController.sendVirtualStickFlightControlData(new FlightControlData(mPitch, mRoll, mYaw, mThrottle), new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if(djiError != null){
                    Log.d(TAG,"send data error is : " + djiError.getDescription().toString());
                }
                else {
                    //Log.d(TAG,"send successed");
                }
            }});
        Log.d(TAG,"turn right signal");
    }


    // right joystick
    @Override
    public void forward() {
        mPitch = (float)0;
        mRoll = (float)1;
        mYaw=0; // ȸ��
        mThrottle=0; // ����
//        if (null == sendVirtualStickDataTimer) {
//            sendVirtualStickDataTask = new SendVirtualStickDataTask();
//            sendVirtualStickDataTimer = new Timer();
//            sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 0, 200);
//        }
        flightController.sendVirtualStickFlightControlData(new FlightControlData(mPitch, mRoll, mYaw, mThrottle), new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if(djiError != null){
                    Log.d(TAG,"send data error is : " + djiError.getDescription().toString());
                }
                else {
                    //Log.d(TAG,"send successed");
                }
            }});
        Log.d(TAG,"forward signal");
    }

    @Override
    public void back() {
        mPitch = (float)0;
        mRoll = -(float)1;
        mYaw=0; // ȸ��
        mThrottle=0; // ����
//        if (null == sendVirtualStickDataTimer) {
//            sendVirtualStickDataTask = new SendVirtualStickDataTask();
//            sendVirtualStickDataTimer = new Timer();
//            sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 0, 200);
//        }
        flightController.sendVirtualStickFlightControlData(new FlightControlData(mPitch, mRoll, mYaw, mThrottle), new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if(djiError != null){
                    Log.d(TAG,"send data error is : " + djiError.getDescription().toString());
                }
                else {
                    //Log.d(TAG,"send successed");
                }
            }});
        Log.d(TAG,"back signal");
    }

    @Override
    public void left() {
        mPitch = -(float)1;
        mRoll = (float)0;
        mYaw=0; // ȸ��
        mThrottle=0; // ����
//        if (null == sendVirtualStickDataTimer) {
//            sendVirtualStickDataTask = new SendVirtualStickDataTask();
//            sendVirtualStickDataTimer = new Timer();
//            sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 0, 200);
//        }
        flightController.sendVirtualStickFlightControlData(new FlightControlData(mPitch, mRoll, mYaw, mThrottle), new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if(djiError != null){
                    Log.d(TAG,"send data error is : " + djiError.getDescription().toString());
                }
                else {
                    //Log.d(TAG,"send successed");
                }
            }});
        Log.d(TAG,"left signal");
    }

    @Override
    public void right() {
        mPitch = (float)1;
        mRoll = (float)0;
        mYaw=0; // ȸ��
        mThrottle=0; // ����
//        if (null == sendVirtualStickDataTimer) {
//            sendVirtualStickDataTask = new SendVirtualStickDataTask();
//            sendVirtualStickDataTimer = new Timer();
//            sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 0, 200);
//        }
        flightController.sendVirtualStickFlightControlData(new FlightControlData(mPitch, mRoll, mYaw, mThrottle), new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if(djiError != null){
                    Log.d(TAG,"send data error is : " + djiError.getDescription().toString());
                }
                else {
                    //Log.d(TAG,"send successed");
                }
            }});
        Log.d(TAG,"right signal");
    }

    @Override
    public void takeOff(){
        //initController();
        Log.d(TAG,"take-Off signal!");
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
       // initController();
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
            Log.d(TAG, "landing signal!");
        }
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(mContext, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG,"onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public boolean isConnect() {
        return connect;
    }

    public void setConnect(boolean connect) {
        this.connect = connect;
    }

    private class SendVirtualStickDataTask extends TimerTask {

        @Override
        public void run() {
            flightController.sendVirtualStickFlightControlData(new FlightControlData(mPitch, mRoll, mYaw, mThrottle), new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if(djiError != null){
                        Log.d(TAG,"send data error is : " + djiError.getDescription().toString());
                    }
                    else {
                        //Log.d(TAG,"send successed");
                    }
                }});
        }
    }
}
