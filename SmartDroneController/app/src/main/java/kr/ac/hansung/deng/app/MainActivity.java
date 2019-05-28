package kr.ac.hansung.deng.app;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

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

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = MainActivity.class.getSimpleName();
    private CustomDroneSDKManager sdkManager;
    private DroneInfoManager droneInfoManager;
    private EmergencyLandingManager emergencyLandingManager;
    private RelativeLayout myLayout;
    private TextView heightText;
    private ImageButton takeOffBtn, landingBtn, emergencyBtn, captureBtn;
    private ImageButton btnUp, btnDown, btnForward, btnBack, btnLeft, btnRight;
    private SeekBar mSeekBar;
    private int touched = 0;

    // Codec for video live view

    // 방향
    //private Button left, right, forward, back, up, down;

    protected TextureView mVideoSurface = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        sdkManager = CustomDroneSDKManager.getInstance();
        sdkManager.setContext(this);
        sdkManager.initController();

        initUI();

        takeOffBtn = (ImageButton) findViewById(R.id.btn_takeoff);
        landingBtn = (ImageButton) findViewById(R.id.btn_landing);
        emergencyBtn = (ImageButton) findViewById(R.id.btn_emergency);
        captureBtn = (ImageButton) findViewById(R.id.btn_capture);

        btnUp = (ImageButton) findViewById(R.id.btn_up);
        btnDown = (ImageButton) findViewById(R.id.btn_down);
        btnForward = (ImageButton) findViewById(R.id.btn_forward);
        btnBack = (ImageButton) findViewById(R.id.btn_back);
        btnLeft = (ImageButton) findViewById(R.id.btn_left);
        btnRight = (ImageButton) findViewById(R.id.btn_right);

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);

        mVideoSurface = (TextureView) findViewById(R.id.video_previewer_surface);
        Log.d(TAG, "mVideoSurface : " + mVideoSurface);
        if (sdkManager != null) {
            // Main Activity �작
            droneInfoManager = DroneInfoManager.getInstance();
            droneInfoManager.init(sdkManager);

            emergencyLandingManager = EmergencyLandingManager.getInstance();
            emergencyLandingManager.init(sdkManager);
            emergencyLandingManager.setActivity(this);
        } else {
            Toast.makeText(this, "from" + TAG + " error : SDK is Null", Toast.LENGTH_SHORT);
            finish();
        }

        sdkManager.getVideo(mVideoSurface);
//        Thread th = new Thread(){
//            @Override
//            public void run() {
//                while(true){
//                    try{
//                        sleep(1000);
//                        heightText.setText(Float.toString(sdkManager.getAircraftHeight()));
//                    }catch (Exception e){
//                        Log.d(TAG,e.getMessage());
//                    }
//                }
//            }
//        };
//
//        th.start();

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    if(seekBar.getProgress() < 50){
                        touched = -1;
                    }
                    else if(seekBar.getProgress() > 50){
                        touched = 1;

                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(50);
                if(touched < 0){
                    sdkManager.moveGimbalDown();
                    //Toast.makeText(getApplicationContext(), "Drone Gimbal moved down!", Toast.LENGTH_SHORT).show();
                }
                else if(touched > 0){
                    sdkManager.moveGimbalUp();
                    //Toast.makeText(getApplicationContext(), "Drone Gimbal moved up!", Toast.LENGTH_SHORT).show();
                }
                touched = 0;
            }
        });
    }
    public void initUI(){
        btnUp = (ImageButton)findViewById(R.id.btn_up);
        btnUp.setOnClickListener(this);
        btnDown = (ImageButton)findViewById(R.id.btn_down);
        btnDown.setOnClickListener(this);
        btnLeft = (ImageButton)findViewById(R.id.btn_left);
        btnLeft.setOnClickListener(this);
        btnRight = (ImageButton)findViewById(R.id.btn_right);
        btnRight.setOnClickListener(this);
        btnForward = (ImageButton)findViewById(R.id.btn_forward);
        btnForward.setOnClickListener(this);
        btnBack = (ImageButton)findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);
        heightText = (TextView)findViewById(R.id.height);
    }

    @Override
    public void onClick(View v) {
        //super.onWindowFocusChanged(hasFocus);

        if (sdkManager != null) {
            switch (v.getId()) {

                case R.id.btn_up: {
                    sdkManager.up();
                    break;
                }
                case R.id.btn_down: {
                    sdkManager.down();
                    break;
                }
                case R.id.btn_left: {
                    sdkManager.left();
                    break;
                }
                case R.id.btn_right: {
                    sdkManager.right();
                    break;
                }
                case R.id.btn_forward: {
                    sdkManager.forward();
                    break;
                }
                case R.id.btn_back: {
                    sdkManager.back();
                    break;
                }
                default:
                    break;
            }
        }
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
        takeOffBtn.setVisibility(View.INVISIBLE);
        landingBtn.setVisibility(View.INVISIBLE);
        emergencyBtn.setVisibility(View.INVISIBLE);
        captureBtn.setVisibility(View.INVISIBLE);

        btnUp.setVisibility(View.INVISIBLE);
        btnDown.setVisibility(View.INVISIBLE);
        btnForward.setVisibility(View.INVISIBLE);
        btnBack.setVisibility(View.INVISIBLE);
        btnLeft.setVisibility(View.INVISIBLE);
        btnRight.setVisibility(View.INVISIBLE);

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

    /*public void onClickDown(View view){
        sdkManager.moveGimbalDownAll();
    }

    public void onClickUp(View view){
        sdkManager.moveGimbalUpAll();
    }*/

    public CustomDroneSDKManager getSdkManager() {
        return sdkManager;
    }

    public TextView getHeightText() {
        return heightText;
    }

    public TextureView getmVideoSurface() {
        return mVideoSurface;
    }
}
