package kr.ac.hansung.deng.activity;

import android.content.Intent;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import kr.ac.hansung.deng.app.CustomDroneSDKManager;
import kr.ac.hansung.deng.app.MainActivity;
import kr.ac.hansung.deng.manager.impl.DroneSDKManager;
import kr.ac.hansung.deng.smartdronecontroller.R;

public class ConnectActivity extends AppCompatActivity {
    private final String TAG = ConnectActivity.class.getSimpleName();
    private DroneSDKManager sdkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        sdkManager = new CustomDroneSDKManager();
    }

    // 드론 연결 시도 버튼
    public void onConnect(View view){
        // sdkManager로 드론과 연결 시도
        sdkManager.connect();

        // main activity로 연결
        Intent intent = new Intent(this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("sdkManager", sdkManager);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
