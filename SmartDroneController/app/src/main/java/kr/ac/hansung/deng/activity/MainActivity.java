package kr.ac.hansung.deng.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import kr.ac.hansung.deng.driver.DJISDKDriver;
import kr.ac.hansung.deng.manager.DroneInfoManager;
import kr.ac.hansung.deng.manager.EmergencyLandingManager;
import kr.ac.hansung.deng.manager.SDKManager;
import kr.ac.hansung.deng.smartdronecontroller.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = MainActivity.class.getSimpleName();

    // SDK Driver
    private SDKManager sdkManager;
    private DroneInfoManager droneInfoManager;

    // Emergency Service
    private EmergencyLandingManager emergencyLandingManager;

    // Component
    private ImageButton takeOffBtn, landingBtn, emergencyBtn, captureBtn;
    private ImageButton btnUp, btnDown, btnForward, btnBack, btnLeft, btnRight;
    private SeekBar mSeekBar;
    private TextView heightText;

    private int touched = 0;

    protected TextureView mVideoSurface = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        sdkManager = DJISDKDriver.getInstance();
        ((DJISDKDriver)sdkManager).setContext(this);
        ((DJISDKDriver)sdkManager).initController();

        initUI();

        this.getSupportActionBar().hide();

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);

        mVideoSurface = (TextureView) findViewById(R.id.video_previewer_surface);
        Log.d(TAG, "mVideoSurface : " + mVideoSurface);
        if (sdkManager != null) {
            // Main Activity �옉
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
        takeOffBtn = (ImageButton) findViewById(R.id.btn_takeoff);
        landingBtn = (ImageButton) findViewById(R.id.btn_landing);
        emergencyBtn = (ImageButton) findViewById(R.id.btn_emergency);
        captureBtn = (ImageButton) findViewById(R.id.btn_capture);

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
        this.finish();
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

    public SDKManager getSdkManager() {
        return sdkManager;
    }

    public TextView getHeightText() {
        return heightText;
    }

    public TextureView getmVideoSurface() {
        return mVideoSurface;
    }
}
