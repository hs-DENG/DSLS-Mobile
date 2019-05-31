package kr.ac.hansung.deng.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.Image;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.TextureView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.security.auth.login.LoginException;

import kr.ac.hansung.deng.ML.ImageClassifier;
import kr.ac.hansung.deng.ML.ImageClassifierFloatInception;
import kr.ac.hansung.deng.app.MainActivity;
import kr.ac.hansung.deng.manager.CustomDroneSDKManager;
import kr.ac.hansung.deng.manager.EmergencyLandingManager;
import kr.ac.hansung.deng.manager.SDKManager;
import kr.ac.hansung.deng.model.ImageLabelInfo;
import kr.ac.hansung.deng.smartdronecontroller.R;
import kr.ac.hansung.deng.util.ImageDivide;
import kr.ac.hansung.deng.view.EmergencyView;

import static java.lang.Thread.sleep;

public class EmergencyService extends Service {

    private static final String TAG = EmergencyService.class.getSimpleName();
    private Thread mThread = null;
    private int mCount = 0;

    // ?습 ?드
    private Bitmap testData;
    private List<Bitmap> divededImages;
    private List<Bitmap> processedImages;
    private ImageClassifier classifier;
    private MainActivity mainActivity;
    private SDKManager sdkManager;

    // safe/unsafe picture info
    private EmergencyView emergencyView;

    // model
    private List<ImageLabelInfo> labelInfoList = new ArrayList<ImageLabelInfo>();
    public EmergencyService() {
        sdkManager = CustomDroneSDKManager.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        // ?레
        if(mThread == null){
            mThread = new Thread("My Thread"){
                @Override
                public void run(){
                    // ??지 ?보 받기 ( ?론 ?보 ?비?로부
                    // 캡쳐 ??지 모델?리?

                    try {

                        processedImages = new ArrayList<Bitmap>();
                        classifier = new ImageClassifierFloatInception(mainActivity);

                        classifier.setNumThreads(1);

                        SpannableStringBuilder textToShow = new SpannableStringBuilder();
                        //Bitmap bitmap = textureView.getBitmap(classifier.getImageSizeX(), classifier.getImageSizeY())
                        //?기카메비트맵이미?르아?성?었classifier(dengception)객체?게 ?시 classifyFrame() ?출?킴


                        float height=0;
                        // ?이 맞추?
                        while (true) {
                            if (height < 5) break;
                            height = sdkManager.getAircraftHeight(); // ?이 가?오?
                            sleep(2000);
                            sdkManager.down();
                            sleep(2000);
                        }
                        Log.d(TAG,"?이 맞추긱공! ?이 : " + height);
                        height=5;

                        // 카메짐볼 ?리?
                        ((CustomDroneSDKManager) sdkManager).moveGimbalDownAll();
                        sleep(5000);
                        Log.d(TAG,"짐볼 ?리긱공! ");

                        //캡처
                        sdkManager.getCapture(mainActivity.getmVideoSurface());
                        testData = ((CustomDroneSDKManager) sdkManager).getCaptureView();
                        Log.d(TAG,"캡처 ?공");

                        ImageDivide divide = new ImageDivide(testData, (int) height); // ??지 divide ?이 만큼 divide
                        divide.cropImage(); // divide ?행
                        Log.d(TAG,"??지 분할 ?공");
                        divededImages = divide.getCroppedImages(); // divide 결과 리스가?오?
                        Log.d(TAG,"??지 분할 결과 가?오긱공");

                        for (Bitmap image : divededImages) {
                            processedImages.add(Bitmap.createScaledBitmap(image, classifier.getImageSizeX(), classifier.getImageSizeY(), true)); // 리사?즈 ?서 벡터?

                        }
                        Log.d(TAG,"??지 리사?즈 ?공");

                        // 모델 ?작
                        int count=0, row=0, col=0;
                        for(Bitmap image: processedImages){
                            classifier.classifyFrame(image, textToShow);
                            col = (int) (count % height);
                            row = (int) (count / height);
                            Log.d(TAG,"row , col = [" + row + ", " + col + "] count = " + count);
                            labelInfoList.add(new ImageLabelInfo(classifier.getLabelProcess().getLabelList().get(0).getKey(),row,col));
                            count++;
                        }
                        Log.d(TAG,"리사?즈??지 ?벨 분류 ?공");

                       // List<Map.Entry<String,Float>> labelList = classifier.getLabelProcess().getLabelList();

                       // Log.d(TAG,"?벨 리스가?오긱공");

                        //가가까운 safe zone ?덱찾아가?오?

                        //TODO  CustomObject shortestPathDetection(labelList);
                        ImageLabelInfo labelInfo = shortestPathDetection(labelInfoList);
                        Log.d(TAG,"최단 경로 계산 ?공 LabelInfo is : " + labelInfo.toString());

                        //TODO ?진 분할 safe/unsafe
                        emergencyView = new EmergencyView(mainActivity, testData, labelInfoList);

                        // Landing
                        //TODO 거리계산( ?제 ?어 ?수 계산 ) ?동 착륙 smartLanding(CustomObject);
                        smartLanding(labelInfo,labelInfoList);
                        Log.d(TAG,"경로 ?동, 착? ?공");

                        // ?원 ?제
                        testData.recycle();
                        for (Bitmap image : divededImages) {
                            image.recycle();
                        }
                        for(Bitmap image: processedImages){
                           image.recycle();
                        }
                        Log.d(TAG,"?원 ?제 ?공");
                    }catch (Exception e){
                        e.printStackTrace();
                        Log.e(TAG,e.getMessage());
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
    public ImageLabelInfo shortestPathDetection(List<ImageLabelInfo> imageLabelInfo){
        int center = imageLabelInfo.size() / 2 ;
        int centerRow = imageLabelInfo.get(center).getRow();
        int centerCol = imageLabelInfo.get(center).getCols();

        ImageLabelInfo min = null;//TODO 모두가 unsafe 경우 ?외처리르야
        double shortestPath = 1000;
        Log.d(TAG, "imageLabelInfo size : " + imageLabelInfo.size());
        // 최단 경로 계산
        for(int i=0;i<imageLabelInfo.size();i++){
            ImageLabelInfo labelInfo = imageLabelInfo.get(i);
            Log.d(TAG, "image Label Info row col = [" + imageLabelInfo.get(i).getRow() + ", " + imageLabelInfo.get(i).getCols() + "]");
            Log.d(TAG,"label : " + labelInfo.getKey());
        }
        for(ImageLabelInfo labelInfo : imageLabelInfo){
            if(labelInfo.getKey().equals("safe")){
               if(shortestPath > (Math.abs(labelInfo.getRow() - centerRow) + Math.abs(labelInfo.getCols() - centerCol))){

                   shortestPath = (Math.abs(labelInfo.getRow() - centerRow) + Math.abs(labelInfo.getCols() - centerCol));
                   Log.d(TAG,"Shortest Path : " + shortestPath);
                   min = labelInfo;
                }
            }
        }
        if(min ==null ) {
            min = new ImageLabelInfo("safe",2,2);
            Log.d(TAG,"There are no safeArea ... just landing");
        }
        return min;
    }

    public void smartLanding(ImageLabelInfo labelInfo, List<ImageLabelInfo> imageLabelInfo){
        int center = imageLabelInfo.size() / 2 ;
        int centerRow = imageLabelInfo.get(center).getRow();
        int centerCol = imageLabelInfo.get(center).getCols();

        if(labelInfo != null){
            if(centerRow > labelInfo.getRow()){
                for(int i=0; i<Math.abs(centerRow - labelInfo.getRow()); i++) {
                    try {
                        Thread.sleep(2000);
                        sdkManager.left();
                    }catch (Exception e){

                    }
                }
            }else if (centerRow < labelInfo.getRow()){
                for(int i=0; i< Math.abs(centerRow - labelInfo.getRow()); i++) {
                    try {
                        Thread.sleep(2000);
                        sdkManager.right();
                    }catch (Exception e){

                    }
                }
            }

            if(centerCol > labelInfo.getCols()){
                for(int i=0; i<Math.abs(centerCol - labelInfo.getCols()); i++) {
                    try {
                        Thread.sleep(2000);
                        sdkManager.forward();
                    }catch (Exception e){

                    }
                }
            } else if (centerCol < labelInfo.getCols()) {
                for(int i=0; i<Math.abs(centerCol - labelInfo.getCols()); i++) {
                    try {
                        Thread.sleep(2000);
                        sdkManager.back();
                    }catch (Exception e){
                    }
                }
            }
            try {
                sleep(3000);
                sdkManager.landing();

            }catch (Exception e){

            }
        }
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

}