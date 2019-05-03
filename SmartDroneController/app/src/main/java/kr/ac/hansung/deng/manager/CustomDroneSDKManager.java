package kr.ac.hansung.deng.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
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
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.product.Model;
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
    public void getVideo(TextureView textureView){
        Log.d("CustomDroneSDKManager","get video signal!");
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
                                Log.d(TAG, "2");
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
        Log.d(TAG, "initPreviewer() 시작");

        BaseProduct product = FPVApplication.getProductInstance();

        if(product == null || !product.isConnected()){
            //showToast(getString(R.string.disconnected));
            Log.d(TAG, "if 들어옴");
        }else {
            Log.d(TAG, "else 들어옴");
            if(myVideoSurface != null){
                myVideoSurface.setSurfaceTextureListener(this);
            }
            if(!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)){
                Log.d(TAG, "else 리스너 ");
                VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(mReceivedVideoDataListener);
            }
        }
    }
    @Override
    public void removeVideo(){
        Log.d("CustomDroneSDKManager","removeVideo() signal!");
        Camera camera = FPVApplication.getCameraInstance();
        if(camera != null){
            //Reset the callback
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(null);
        }
    }

    @Override
    public void getCapture(TextureView textureView){
        Log.d("CustomDroneSDKManager","getCapture() signal!");

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
            Log.d("CustomDroneSDKManager","capture success");
            Log.d("CustomDroneSDKManager", strFilePath);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        /*final Camera camera = FPVApplication.getCameraInstance();

        handler = new Handler();

        if (camera != null) {

            camera.setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        Log.d("CustomDroneSDKManager", "camera mode setting success!");
                    }
                }
            });

            SettingsDefinitions.ShootPhotoMode photoMode = SettingsDefinitions.ShootPhotoMode.SINGLE; // Set the camera capture mode as Single mode

            camera.setShootPhotoMode(photoMode, new CommonCallbacks.CompletionCallback(){
                @Override
                public void onResult(DJIError djiError) {
                    if (null == djiError) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                camera.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        if (djiError == null) {
                                            Log.d("CustomDroneSDKManager","take photo: success");
                                            Toast.makeText(mContext, "take photo : success", Toast.LENGTH_LONG);

                                        } else {
                                            Log.d("CustomDroneSDKManager","djiError : " + djiError.getDescription());
                                            Toast.makeText(mContext, "djiError : " + djiError, Toast.LENGTH_LONG);
                                        }
                                    }
                                });
                            }
                        }, 2000);
                    }
                }
            });
        }*/





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

