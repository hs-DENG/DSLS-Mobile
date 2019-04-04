package kr.ac.hansung.deng.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import kr.ac.hansung.deng.activity.EmergencyActivity;
import kr.ac.hansung.deng.manager.DroneSDKManager;
import kr.ac.hansung.deng.smartdronecontroller.R;

public class MainActivity extends AppCompatActivity {
    private final String TAG =MainActivity.class.getSimpleName();
    private DroneSDKManager sdkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // sdk 연결
    }

    //take off button
    public void onClickTakeOff(View view){
        sdkManager.takeOffDrone();
    }

    //landing button
    public void onClickLanding(View view){
        sdkManager.landingDrone();
    }

    //emergency button
    public void onClickEmergency(View view){
        Intent intent = new Intent(this, EmergencyActivity.class);
        startActivity(intent);
    }
}
