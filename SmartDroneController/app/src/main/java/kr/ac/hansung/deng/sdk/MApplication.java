package kr.ac.hansung.deng.sdk;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

/**
 * Created by khb on 2019-04-05.
 */

public class MApplication extends Application {

    private FPVApplication fpvApplication;

    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);
        if (fpvApplication == null) {
            fpvApplication = new FPVApplication();
            fpvApplication.setContext(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fpvApplication.onCreate();
    }

}
