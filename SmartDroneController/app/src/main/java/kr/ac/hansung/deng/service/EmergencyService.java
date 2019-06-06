package kr.ac.hansung.deng.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.text.SpannableStringBuilder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import kr.ac.hansung.deng.ML.ImageClassifier;
import kr.ac.hansung.deng.ML.ImageClassifierFloatInception;
import kr.ac.hansung.deng.activity.MainActivity;
import kr.ac.hansung.deng.driver.DJISDKDriver;
import kr.ac.hansung.deng.manager.SDKManager;
import kr.ac.hansung.deng.model.ImageLabelInfo;
import kr.ac.hansung.deng.util.ImageDivide;

import static java.lang.Thread.sleep;

public class EmergencyService extends Service {

    private static final String TAG = EmergencyService.class.getSimpleName();

    // service thread
    private Thread mThread = null;

    // for drone control
    private MainActivity mainActivity;
    private SDKManager sdkManager;

    // reference that for run learning model
    private Bitmap testData;
    private List<Bitmap> divededImages;
    private List<Bitmap> processedImages;
    private ImageClassifier classifier;

    // tool for drawing divided section of safe/unsafe information
    private Canvas canvas;
    private final static int line = Color.BLACK;
    private final static int safeArea = Color.GREEN;
    private final static int unsafeArea = Color.RED;

    // reference for calc shortest path
    int count=0, row=0, cols =0;

    // model
    private List<ImageLabelInfo> labelInfoList = new ArrayList<ImageLabelInfo>();
    public EmergencyService() {
        sdkManager = DJISDKDriver.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        if(mThread == null){
            mThread = new Thread("My Thread"){
                @Override
                public void run(){
                    try {
                        processedImages = new ArrayList<Bitmap>();
                        float height=0;
                        while (true) {
                            if (height < 5) break;
                            height = sdkManager.getAircraftHeight(); // �이 가�오�
                            sleep(2000);
                            sdkManager.down();
                            sleep(2000);
                        }
                        height=5;

                        // 카메짐볼 �리�
                        ((DJISDKDriver) sdkManager).moveGimbalDownAll();
                        sleep(5000);
                        Log.d(TAG,"camera gimbal down all");
                        //캡처
                        sdkManager.getCapture(mainActivity.getmVideoSurface());
                        testData = ((DJISDKDriver) sdkManager).getCaptureView();
                        Log.d(TAG,"using camera capture function for get area data");

                        ImageDivide divide = new ImageDivide(testData, (int) height); // ��지 divide �이 만큼 divide
                        divide.cropImage(); // divide �행
                        divededImages = divide.getCroppedImages(); // divide 결과 리스가�오�
                        Log.d(TAG,"area data divide");

                        for (Bitmap image : divededImages) {
                            processedImages.add(Bitmap.createScaledBitmap(image, 299,299, true)); // 리사�즈 �서 벡터�
                        }

                        // 모델 �작

                        for(Bitmap image: processedImages){
                            classifier = new ImageClassifierFloatInception(mainActivity);
                            classifier.setNumThreads(1);
                            SpannableStringBuilder textToShow = new SpannableStringBuilder();
                            classifier.classifyFrame(image, textToShow);
                            cols = (int) (count % height);
                            row = (int) (count / height);
                            Log.d(TAG,"row , col = [" + row + ", " + cols + "] count = " + count);
                            labelInfoList.add(new ImageLabelInfo(classifier.getLabelProcess().getLabelList().get(0).getKey(),row,cols));
                            count++;
                            classifier.close();
                        }

                        //CustomObject shortestPathDetection(labelList);
                        ImageLabelInfo labelInfo = shortestPathDetection(labelInfoList);

                        //safe/unsafe
                        drawAreaSection();

                        // Landing

                        // Calculate Shortest Path ( with greedy algorythm)
                        smartLanding(labelInfo,labelInfoList);

                        // Release Resources
                        count = row = cols = 0 ;

                        testData.recycle();
                        for (Bitmap image : divededImages) {
                            image.recycle();
                        }
                        for(Bitmap image: processedImages){
                            image.recycle();
                        }

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
        int centerCols = imageLabelInfo.get(center).getCols();

        ImageLabelInfo min = null;//TODO 모두가 unsafe 경우 �외처리르야
        double shortestPath = 1000;
        Log.d(TAG, "imageLabelInfo size : " + imageLabelInfo.size());
        // 최단 경로 계산
        for(int i=0;i<imageLabelInfo.size();i++){
            ImageLabelInfo labelInfo = imageLabelInfo.get(i);
            Log.d(TAG, "image Label Info row cols = [" + imageLabelInfo.get(i).getRow() + ", " + imageLabelInfo.get(i).getCols() + "]");
            Log.d(TAG,"label : " + labelInfo.getKey());
        }
        for(ImageLabelInfo labelInfo : imageLabelInfo){
            if(labelInfo.getKey().equals("safe")){
                if(shortestPath > (Math.abs(labelInfo.getRow() - centerRow) + Math.abs(labelInfo.getCols() - centerCols))){

                    shortestPath = (Math.abs(labelInfo.getRow() - centerRow) + Math.abs(labelInfo.getCols() - centerCols));
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
    public void drawAreaSection(){
        canvas = new Canvas(testData);

        Paint mPaint;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(line);
        mPaint.setAntiAlias(true);

        //canvas.drawBitmap(testData,10,10,mPaint);
        int safeIndex = 0;
        int startX=0, startY=0;
        int stopX=0, stopY=0, startWidth =0, startHeight =0, stopWidth= 0, stopHeight = 0;
        for(int i=0; i<4; i++){
            startWidth = 0;
            startHeight = (testData.getHeight() / 5) * (i+1);
            stopWidth = testData.getWidth();
            stopHeight = (testData.getHeight() / 5) * (i+1);
            canvas.drawLine(startWidth, startHeight, stopWidth, stopHeight, mPaint);
        }
        for(int i=0; i<4; i++){
            startWidth = (testData.getWidth() / 5) * (i+1);
            startHeight = 0;
            stopWidth = (testData.getWidth() / 5) * (i+1);
            stopHeight = testData.getHeight();
            canvas.drawLine(startWidth,startHeight,stopWidth,stopHeight, mPaint);
        }

        for (ImageLabelInfo label : labelInfoList) {
            if (label.getKey().equals("safe")) {
                mPaint.setColor(safeArea);
            }
            else {
                mPaint.setColor(unsafeArea);
            }
            safeIndex = labelInfoList.indexOf(label);
            startX = (safeIndex % 5) * (canvas.getWidth() / 5);
            startY = (safeIndex / 5) * (canvas.getHeight() / 5);
            stopX = startX + (canvas.getWidth() / 5);
            stopY = startY + (canvas.getHeight() / 5);
            mPaint.setAlpha(60);
            canvas.drawRect(startX, startY, stopX, stopY, mPaint);
        }
        String strFolderPath = Environment.getExternalStorageDirectory() + "/Pictures/SDCE";

        File myFile = new File(strFolderPath);

        if(!myFile.exists()) {
            myFile.mkdirs();
        }

        FileOutputStream fos;

        String strFilePath = strFolderPath + "/" + System.currentTimeMillis() + ".png";
        File fileCacheItem = new File(strFilePath);

        try {
            fos = new FileOutputStream(fileCacheItem);
            testData.compress(Bitmap.CompressFormat.PNG, 100, fos);

            //this code will scan the image so that it will appear in your gallery when you open next time
            MediaScannerConnection.scanFile(mainActivity, new String[] { fileCacheItem.toString() }, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.d("appname", "image is saved in gallery and gallery is refreshed.");
                        }
                    }
            );

            mainActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://"+ strFilePath)));
            Log.d("EmergencyView","capture success");
            Log.d("EmergencyView", strFilePath);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    public void smartLanding(ImageLabelInfo labelInfo, List<ImageLabelInfo> imageLabelInfo){
        int center = imageLabelInfo.size() / 2 ;
        int centerRow = imageLabelInfo.get(center).getRow();
        int centerCols = imageLabelInfo.get(center).getCols();

        if(labelInfo != null){
            if(centerRow > labelInfo.getRow()){
                for(int i=0; i<Math.abs(centerRow - labelInfo.getRow()); i++) {
                    try {
                        Thread.sleep(2000);
                        sdkManager.forward();
                        Thread.sleep(2000);
                        sdkManager.forward();
                    }catch (Exception e){

                    }
                }
            }else if (centerRow < labelInfo.getRow()){
                for(int i=0; i< Math.abs(centerRow - labelInfo.getRow()); i++) {
                    try {
                        Thread.sleep(2000);
                        sdkManager.back();
                        Thread.sleep(2000);
                        sdkManager.back();
                    }catch (Exception e){

                    }
                }
            }

            if(centerCols > labelInfo.getCols()){
                for(int i=0; i<Math.abs(centerCols - labelInfo.getCols()); i++) {
                    try {
                        Thread.sleep(2000);
                        sdkManager.left();
                        Thread.sleep(2000);
                        sdkManager.left();
                    }catch (Exception e){

                    }
                }
            } else if (centerCols < labelInfo.getCols()) {
                for(int i=0; i<Math.abs(centerCols - labelInfo.getCols()); i++) {
                    try {
                        Thread.sleep(2000);
                        sdkManager.right();
                        Thread.sleep(2000);
                        sdkManager.right();
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

    public void setActivity(MainActivity activity){
        this.mainActivity = activity;

    }

    public ImageClassifier getClassifier() {
        return classifier;
    }
}