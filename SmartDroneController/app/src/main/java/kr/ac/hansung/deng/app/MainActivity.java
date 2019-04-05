package kr.ac.hansung.deng.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import kr.ac.hansung.deng.manager.DroneInfoManager;
import kr.ac.hansung.deng.manager.SDKManager;
import kr.ac.hansung.deng.manager.impl.DroneSDKManager;
import kr.ac.hansung.deng.smartdronecontroller.R;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    private DroneSDKManager sdkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        sdkManager = (DroneSDKManager) intent.getSerializableExtra("sdkManager");
        if(sdkManager != null){
            // Main Activity 시작
            DroneInfoManager droneInfoManager = new DroneInfoManager();

        }
        else{
            Toast.makeText(this, "from" + TAG + " error : SDK is Null", Toast.LENGTH_SHORT);
            finish();
        }
    }

    //take off button
    public void onClickTakeOff(View view){ sdkManager.takeOff(); }

    //landing button
    public void onClickLanding(View view){
        sdkManager.landing();
    }

    //emergency button
    public void onClickEmergency(View view){
        //TODO Do Smart Landing
    }


    public DroneSDKManager getSdkManager() {
        return sdkManager;
    }
}
