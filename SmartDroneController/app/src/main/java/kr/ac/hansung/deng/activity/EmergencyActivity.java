package kr.ac.hansung.deng.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import kr.ac.hansung.deng.service.EmergencyService;
import kr.ac.hansung.deng.smartdronecontroller.R;

public class EmergencyActivity extends AppCompatActivity {
    private final String TAG = EmergencyActivity.class.getSimpleName();
    private EmergencyService myService = null;
    private boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            EmergencyService.MyBinder binder = (EmergencyService.MyBinder) service;
            myService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    // 앱이 동작 중일 때만 바인드
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "emergencyActivity onStart()");
        //서비스와 연결
        Intent intent = new Intent(this, EmergencyService.class);
        startService(intent);
        // 바인딩되면 서비스 자동으로 시작
        bindService(intent, mConnection, BIND_AUTO_CREATE);

    }

    public void onClick(View view){
        if(mBound)
            Toast.makeText(this, "카운팅 : "
                    + myService.getCount(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "emergencyActivity onStop()");
        //서비스와 연결 해제
        if(mBound){
            unbindService(mConnection);
            mBound = false;
        }
    }
}
