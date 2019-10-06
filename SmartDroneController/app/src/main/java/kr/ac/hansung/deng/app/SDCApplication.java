package kr.ac.hansung.deng.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.secneo.sdk.Helper;

import kr.ac.hansung.deng.sdk.FPVApplication;

/**
 * Created by rowks on 2019-04-18.
 */

public class SDCApplication extends Application{

//    private CustomDroneSDKManager sdkManager = CustomDroneSDKManager.getInstance();
    private FPVApplication fpvApplication;
    private static SDCApplication context;

    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(SDCApplication.this);
        context = this;
//        if (fpvApplication == null) {
//            fpvApplication = new FPVApplication();
//            fpvApplication.setContext(this);
//
//        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //fpvApplication.onCreate();
  //      sdkManager.setContext(this);
    }

    public static SDCApplication getContext() {
        return context;
    }
}
