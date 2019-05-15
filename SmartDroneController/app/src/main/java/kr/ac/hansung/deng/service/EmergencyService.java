package kr.ac.hansung.deng.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.text.SpannableStringBuilder;
import android.util.Log;

import java.io.IOException;

import kr.ac.hansung.deng.ML.ImageClassifier;
import kr.ac.hansung.deng.ML.ImageClassifierFloatInception;
import kr.ac.hansung.deng.app.MainActivity;
import kr.ac.hansung.deng.manager.CustomDroneSDKManager;
import kr.ac.hansung.deng.manager.EmergencyLandingManager;
import kr.ac.hansung.deng.manager.SDKManager;
import kr.ac.hansung.deng.smartdronecontroller.R;

public class EmergencyService extends Service {

    private static final String TAG = EmergencyService.class.getSimpleName();
    private Thread mThread = null;
    private int mCount = 0;
    private Bitmap bitmap1,bitmap2;
    private ImageClassifier classifier;
    private MainActivity mainActivity;
    private SDKManager sdkManager;

    public EmergencyService() {
        sdkManager = CustomDroneSDKManager.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        // 스레드
        if(mThread == null){
            mThread = new Thread("My Thread"){
                @Override
                public void run(){
                    // 이미지 정보 받기 ( 드론 정보 서비스로부터)
                    // 캡쳐 이미지 모델에 돌리기
                    //

//                    try{
//                        classifier = new ImageClassifierFloatInception(mainActivity);
//                    }catch (IOException e){
//                        Log.e(TAG,"Fail to create ImageClassifier");
//                        classifier = null;
//                    }
//
//                    classifier.setNumThreads(1);
//
//                    SpannableStringBuilder textToShow = new SpannableStringBuilder();
//                    //Bitmap bitmap = textureView.getBitmap(classifier.getImageSizeX(), classifier.getImageSizeY())
//                    //여기서 카메라 비트맵이미지를 담아서 생성되었던 classifier(dengception)객체에게 다시 classifyFrame() 호출시킴
//
//
//                    bitmap2 = Bitmap.createScaledBitmap(bitmap1,classifier.getImageSizeX(),classifier.getImageSizeY(),true);
//                    classifier.classifyFrame(bitmap2, textToShow); //ImageClassifier 클래스 118라인으로 이동
//                    bitmap2.recycle();
//                    bitmap1.recycle();
                        try {
//                            for(int i=0;i<5;i++){
//                                sleep(2000);
//                                sdkManager.moveGimbalDown();
//                            }
                            sdkManager.up();
                            sleep(3000);
                            sdkManager.getGoHomeHeightInMeters();
                            sleep(3000);
                            sdkManager.up();
                            sleep(3000);
                            sdkManager.getGoHomeHeightInMeters();
//                            sleep(3000);
//                            sdkManager.down();
//                            sleep(3000);
//                            sdkManager.left();
//                            sleep(3000);
//                            sdkManager.right();
//                            sleep(3000);
//                            sdkManager.forward();
//                            sleep(3000);
//                            sdkManager.back();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("Emergency Service", "error is : "+e.getMessage());
                        }
                    }
            };



            mThread.start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy()");

        if(mThread != null)
            mThread = null;
    }

    public class MyBinder extends Binder {
        public EmergencyService getService(){
            return EmergencyService.this;
        }
    }

    private IBinder mBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    public int getCount(){
        return mCount;
    }


    public void setActivity(MainActivity activity){
        this.mainActivity = activity;

    }

    public ImageClassifier getClassifier() {
        return classifier;
    }

    public void setClassifier(ImageClassifier classifier) {
        this.classifier = classifier;
    }

    public Bitmap getBitmap1() {
        return bitmap1;
    }

    public void setBitmap1(Bitmap bitmap1) {
        this.bitmap1 = bitmap1;
    }
}