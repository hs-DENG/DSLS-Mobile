package kr.ac.hansung.deng.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import kr.ac.hansung.deng.driver.DJISDKDriver;
import kr.ac.hansung.deng.manager.SDKManager;
import kr.ac.hansung.deng.sdk.FPVApplication;
import kr.ac.hansung.deng.smartdronecontroller.R;

public class ConnectActivity extends Activity implements View.OnClickListener{
    private static final String TAG = ConnectActivity.class.getName();

    private SDKManager sdkManager;

    private Button mBtnOpen;
    private Button mBtnReConnect;

    private LinearLayout myLayout;
    private ImageButton mBtnMenu;
    private PopupWindow popup;
    private View popupView;
    private Button mBtnOk;
    private String company="";

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };

    private Spinner mySpinner;
    private ArrayAdapter spinnerAdapter;

    private List<String> missingPermission = new ArrayList<>();
    //private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static final int REQUEST_PERMISSION_CODE = 12345;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sdkManager = DJISDKDriver.getInstance();
        ((DJISDKDriver)sdkManager).setContext(this);
        checkAndRequestPermissions();
        setContentView(R.layout.activity_connect);

        initUI();

        // Register the broadcast receiver for receiving the device connection's changes.
        IntentFilter filter = new IntentFilter();
        filter.addAction(FPVApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
    }

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */

    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (!missingPermission.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }

    }

    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "requestCode : " + requestCode + " permissions : " + permissions +
                " grantResults : " + grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            sdkManager.connect();
        } else {
            showToast("Missing permissions!!!");
        }
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
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

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void initUI() {

        myLayout = (LinearLayout) findViewById(R.id.myLayout);
        mBtnMenu = (ImageButton) findViewById(R.id.menuButton);

        mBtnOpen = (Button) findViewById(R.id.btn_open);
        mBtnOpen.setOnClickListener(this);
        mBtnOpen.setEnabled(false
        );
        mBtnReConnect = (Button)findViewById(R.id.btn_reConnect);
        mBtnReConnect.setOnClickListener(this);


        String group1 = "DJI";
        String group2 = "Xiaomi";
        String group3 = "Parrot";

        ArrayList<String> groups = new ArrayList<>();
        groups.add(group1);
        groups.add(group2);
        groups.add(group3);

        mySpinner = (Spinner) findViewById(R.id.mySpinner);
        spinnerAdapter = new ArrayAdapter(this, R.layout.item_spinner, groups);
        mySpinner.setAdapter(spinnerAdapter);

        /*mySpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mySpinner.getItemAtPosition(position).equals("DJI"))
                    company = "DJI";
                else if(mySpinner.getItemAtPosition(position).equals("Xiaomi"))
                    company = "Xiaomi";
                else if(mySpinner.getItemAtPosition(position).equals("Parrot"))
                    company = "Parrot";
            }
        });*/

        popupView = View.inflate(this, R.layout.dialog_activity, null);
        popup = new PopupWindow(popupView, 1000, 1000, true);

        mBtnOk = (Button) popupView.findViewById(R.id.okButton);
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshSDKRelativeUI();
        }
    };

    private void refreshSDKRelativeUI() {
        BaseProduct mProduct = FPVApplication.getProductInstance();

        if (null != mProduct && mProduct.isConnected()) {

            Log.v(TAG, "refreshSDK: True");
            mBtnOpen.setEnabled(true);

            String str = mProduct instanceof Aircraft ? "DJIAircraft" : "DJIHandHeld";
            //mTextConnectionStatus.setText("Status: " + str + " connected");

            if (null != mProduct.getModel()) {
                //mTextProduct.setText("" + mProduct.getModel().getDisplayName());
            } else {
                //mTextProduct.setText(R.string.product_information);
            }

        } else {

            Log.v(TAG, "refreshSDK: False");
            mBtnOpen.setEnabled(false);

            //mTextProduct.setText(R.string.product_information);
            //mTextConnectionStatus.setText(R.string.connection_loose);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_open: {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("sdkManager", sdkManager);
//                intent.putExtras(bundle);
                startActivity(intent);
                break;
            }
            case R.id.btn_reConnect:{
               // Toast.makeText(this,"Trying Re Connect",Toast.LENGTH_SHORT).show();
               // Log.d("test","Trying Re Connect");
                sdkManager.connect();
            }
            default:
                break;
        }
    }

    private void showToast(final String toastMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();

            }
        });
    }

    public void mOnClickMenu(View view){
        popup.showAtLocation(myLayout, Gravity.CENTER, 0, 0);
        popup.setAnimationStyle(-1);
        popup.showAsDropDown(mBtnMenu);

    }

}