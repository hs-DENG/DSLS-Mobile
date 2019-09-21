package kr.ac.hansung.deng.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import kr.ac.hansung.deng.activity.MainActivity;
import kr.ac.hansung.deng.model.ImageLabelInfo;

public class ResultDrawer {
    private Canvas canvas;

    private final static int line = Color.BLACK;
    private final static int safeArea = Color.GREEN;
    private final static int unsafeArea = Color.RED;
    private final static int landingArea = Color.YELLOW;

    private boolean landing = false;

    public void drawAreaSection(Context mainActivity, int height, Bitmap testData, List<ImageLabelInfo> labelInfoList){

        canvas = new Canvas(testData);

        Paint mPaint;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(line);
        mPaint.setAntiAlias(true);

        //canvas.drawBitmap(testData,10,10,mPaint);
        int safeIndex = 0;
        int startX=0, startY=0;
        int stopX=0, stopY=0, startWidth =0, startHeight =0, stopWidth= 0, stopHeight = 0;
        for(int i=0; i<height-1; i++){
            startWidth = 0;
            startHeight = (testData.getHeight() / height) * (i+1);
            stopWidth = testData.getWidth();
            stopHeight = (testData.getHeight() / height) * (i+1);
            canvas.drawLine(startWidth, startHeight, stopWidth, stopHeight, mPaint);
        }
        for(int i=0; i<height-1; i++){
            startWidth = (testData.getWidth() / height) * (i+1);
            startHeight = 0;
            stopWidth = (testData.getWidth() / height) * (i+1);
            stopHeight = testData.getHeight();
            canvas.drawLine(startWidth,startHeight,stopWidth,stopHeight, mPaint);
        }

        for (ImageLabelInfo label : labelInfoList) {
            if (label.getKey().equals("safe")) {
                if(!landing){
                    mPaint.setColor(landingArea);
                    landing = true;
                }
                mPaint.setColor(safeArea);
            }
            else {
                mPaint.setColor(unsafeArea);
            }
            startX = label.getCols() * (canvas.getWidth() / height);
            startY = label.getRow() * (canvas.getHeight() / height);
            stopX = startX + (canvas.getWidth() / height);
            stopY = startY + (canvas.getHeight() / height);
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
}