package kr.ac.hansung.deng.app;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Handler;
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
import kr.ac.hansung.deng.manager.SDKManager;
import kr.ac.hansung.deng.sdk.FPVApplication;
import kr.ac.hansung.deng.smartdronecontroller.R;

public class MainActivity extends AppCompatActivity{
    private final String TAG = MainActivity.class.getSimpleName();
    private CustomDroneSDKManager sdkManager;
    private DroneInfoManager droneInfoManager;
    private EmergencyLandingManager emergencyLandingManager;
    // Codec for video live view

    protected TextureView mVideoSurface = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        sdkManager = CustomDroneSDKManager.getInstance();
        sdkManager.setContext(this);
        sdkManager.initController();

        mVideoSurface = (TextureView) findViewById(R.id.video_previewer_surface);
        Log.d(TAG, "mVideoSurface : " + mVideoSurface);
        if(sdkManager != null){
            // Main Activity 시작
            droneInfoManager = DroneInfoManager.getInstance();
            droneInfoManager.init(sdkManager);

            emergencyLandingManager = EmergencyLandingManager.getInstance();
            emergencyLandingManager.init(sdkManager);
            emergencyLandingManager.setActivity(this);
        }
        else{
            Toast.makeText(this, "from" + TAG + " error : SDK is Null", Toast.LENGTH_SHORT);
            finish();
        }

        sdkManager.getVideo(mVideoSurface);
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();

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


        sdkManager.removeVideo();
        if(emergencyLandingManager.getEmergencyService().getClassifier()!= null)
            emergencyLandingManager.getEmergencyService().getClassifier().close();

        super.onDestroy();
    }

    //emergency button
    public void onClickEmergency(View view){
        //TODO Do Smart Landing
        emergencyLandingManager.runService();
    }

    public void onClickTakeoff(View view){
        sdkManager.takeOff();
    }
    public void onClickLand(View view){
        sdkManager.landing();
    }

    public void onClickCapture(View view){
        if(mVideoSurface == null){
            Log.d("MainActivity", "mVideoSurface is null");
        }
        sdkManager.getCapture(mVideoSurface);
    }

    public void onClickDown(View view){
        sdkManager.moveGimbalDown();
    }

    public void onClickUp(View view){
        sdkManager.moveGimbalUp();
    }

    public CustomDroneSDKManager getSdkManager() {
        return sdkManager;
    }

}
