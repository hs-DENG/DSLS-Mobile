package kr.ac.hansung.deng.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.text.SpannableStringBuilder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import kr.ac.hansung.deng.ML.ImageClassifier;
import kr.ac.hansung.deng.ML.ImageClassifierFloatInception;
import kr.ac.hansung.deng.app.MainActivity;
import kr.ac.hansung.deng.manager.CustomDroneSDKManager;
import kr.ac.hansung.deng.manager.EmergencyLandingManager;
import kr.ac.hansung.deng.manager.SDKManager;
import kr.ac.hansung.deng.smartdronecontroller.R;
import kr.ac.hansung.deng.util.ImageDivide;

public class EmergencyService extends Service {

    private static final String TAG = EmergencyService.class.getSimpleName();
    private Thread mThread = null;
    private int mCount = 0;
    private Bitmap bitmap1,testData;
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

                    try{
                        classifier = new ImageClassifierFloatInception(mainActivity);
                    }catch (IOException e){
                        Log.e(TAG,"Fail to create ImageClassifier");
                        classifier = null;
                    }

                    classifier.setNumThreads(1);

                    SpannableStringBuilder textToShow = new SpannableStringBuilder();
                    //Bitmap bitmap = textureView.getBitmap(classifier.getImageSizeX(), classifier.getImageSizeY())
                    //여기서 카메라 비트맵이미지를 담아서 생성되었던 classifier(dengception)객체에게 다시 classifyFrame() 호출시킴


                    testData = Bitmap.createScaledBitmap(bitmap1,classifier.getImageSizeX(),classifier.getImageSizeY(),true); // 모델에 넣기 위한 이미지 리사이즈
                    classifier.classifyFrame(testData, textToShow);

                    float height = sdkManager.getAircraftHeight(); // 높이 가져오기

                    //TODO
                    ImageDivide divide = new ImageDivide(testData,(int)height); // 이미지 divide 높이 만큼 divide
                    divide.cropImage(); // divide 수행
                    List<Bitmap> imagaes =divide.getCroppedImages(); // divide 결과 리스트 가져오기


                    // test
                    Bitmap map = imagaes.get(12);

                    FileOutputStream fos;

                    String strFolderPath = Environment.getExternalStorageDirectory() + "/Pictures/SDC";

                    File myFile = new File(strFolderPath);

                    if(!myFile.exists()) {
                        myFile.mkdirs();
                    }

                    String strFilePath = strFolderPath + "/" + "testData" + ".png";
                    File fileCacheItem = new File(strFilePath);

                    try {
                        fos = new FileOutputStream(fileCacheItem);
                        map.compress(Bitmap.CompressFormat.PNG, 100, fos);

                        mainActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.parse("file://"+ strFilePath)));
                        Log.d(TAG,"capture success");
                        Log.d(TAG, strFilePath);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    testData.recycle();
                    bitmap1.recycle();

                    try {
                        sleep(3000);
                        sdkManager.landing();
                    }catch (Exception e){

                    }
//                        try {
//                            ((CustomDroneSDKManager)sdkManager).moveGimbalDownAll();
//                            sleep(3000);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            Log.d("Emergency Service", "error is : "+e.getMessage());
//                        }
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