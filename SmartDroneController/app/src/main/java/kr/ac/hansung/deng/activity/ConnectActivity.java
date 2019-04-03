package kr.ac.hansung.deng.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import kr.ac.hansung.deng.app.CustomerDroneSDKManager;
import kr.ac.hansung.deng.app.MainActivity;
import kr.ac.hansung.deng.manager.DroneSDKManager;
import kr.ac.hansung.deng.smartdronecontroller.R;

public class ConnectActivity extends AppCompatActivity {

    private DroneSDKManager sdkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        sdkManager = new CustomerDroneSDKManager();
    }

    // 드론 연결 시도 버튼
    public void onConnect(View view){
        // sdkManager로 드론과 연결 시도
        sdkManager.connect();

        // main activity로 연결
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }
}
