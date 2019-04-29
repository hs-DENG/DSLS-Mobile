package kr.ac.hansung.deng.app;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import dji.common.camera.SystemState;
import dji.common.error.DJIError;
import dji.common.product.Model;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.useraccount.UserAccountManager;
import kr.ac.hansung.deng.manager.CustomDroneSDKManager;
import kr.ac.hansung.deng.manager.DroneInfoManager;
import kr.ac.hansung.deng.manager.EmergencyLandingManager;
import kr.ac.hansung.deng.sdk.FPVApplication;
import kr.ac.hansung.deng.smartdronecontroller.R;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener{
    private final String TAG = MainActivity.class.getSimpleName();
    private CustomDroneSDKManager sdkManager;
    private DroneInfoManager droneInfoManager;
    private EmergencyLandingManager emergencyLandingManager;
    // Codec for video live view

    //TODO 코드 옯기기
    protected DJICodecManager mCodecManager = null;
    protected TextureView mVideoSurface = null;
    protected VideoFeeder.VideoDataListener mReceivedVideoDataListener = null;
    //TODO 코드 옯기기

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        sdkManager = CustomDroneSDKManager.getInstance();
        sdkManager.setContext(this);
        if(sdkManager != null){
            // Main Activity 시작
            droneInfoManager = DroneInfoManager.getInstance();
            droneInfoManager.init(sdkManager);

            emergencyLandingManager = EmergencyLandingManager.getInstance();
            emergencyLandingManager.init(sdkManager);
        }
        else{
            Toast.makeText(this, "from" + TAG + " error : SDK is Null", Toast.LENGTH_SHORT);
            finish();
        }
        //TODO 코드 옯기기
        initUI();

        // The callback for receiving the raw H264 video data for camera live view
        mReceivedVideoDataListener = new VideoFeeder.VideoDataListener() {

            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };

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
        //TODO 코드 옯기기
    }

    //TODO 코드 옯기기
    protected void onProductChange() {
        initPreviewer();
        //loginAccount();
    }

    private void loginAccount(){
        Log.d(TAG, "loginAccount() 불림");
        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Log.d(TAG, "Login Success");
                    }
                    @Override
                    public void onFailure(DJIError error) {
                        //showToast("Login Error:" + error.getDescription());
                        Log.d(TAG, "Login Error:" + error.getDescription());
                    }
                });
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        onProductChange();

        if(mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        uninitPreviewer();
        super.onDestroy();
    }

    private void initUI() {
        // init mVideoSurface
        mVideoSurface = findViewById(R.id.video_previewer_surface);
        if(mVideoSurface != null){
            mVideoSurface.setSurfaceTextureListener(this);
            Log.d(TAG, "initUI() 시작 mVideoSurface null 아님");
        }

    }

    private void initPreviewer(){
        BaseProduct product = FPVApplication.getProductInstance();
        Log.d(TAG, "initPreviewer() 시작");
        if(product == null || !product.isConnected()){
            //showToast(getString(R.string.disconnected));
            if(product == null)
                Log.d(TAG, "initPreviewer()에서 product == null");
            else if(!product.isConnected())
                Log.d(TAG, "initPreviewer()에서 product.isConnected() == false");
            Log.d(TAG, getString(R.string.disconnected) + "");
        }else {
            if(mVideoSurface != null){
                mVideoSurface.setSurfaceTextureListener(this);
            }
            if(!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)){
                VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(mReceivedVideoDataListener);
            }
        }
    }

    private void uninitPreviewer(){
        Camera camera = FPVApplication.getCameraInstance();
        if(camera != null){
            //Reset the callback
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(null);
        }
    }
    //TODO 코드 옯기기

    //emergency button
    public void onClickEmergency(View view){
        //TODO Do Smart Landing
    }

    public void onClickTakeoff(View view){
        sdkManager.takeOff();
    }
    public void onClickLand(View view){
        sdkManager.landing();
    }


    public CustomDroneSDKManager getSdkManager() {
        return sdkManager;
    }
    //TODO 코드 옯기기

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
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
    //TODO 코드 옯기기
}
