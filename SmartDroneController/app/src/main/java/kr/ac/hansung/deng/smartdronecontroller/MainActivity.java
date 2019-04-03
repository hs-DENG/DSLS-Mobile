package kr.ac.hansung.deng.smartdronecontroller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import kr.ac.hansung.deng.manager.DroneSDKManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DroneSDKManager sdkManager = new CustomerDroneSDKManager();

        sdkManager.connect();
    }
}
