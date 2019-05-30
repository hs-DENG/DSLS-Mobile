package kr.ac.hansung.deng.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

import kr.ac.hansung.deng.model.ImageLabelInfo;

public class EmergencyView extends View {

    private Paint mPaint;
    private Bitmap bitmap;
    private List<ImageLabelInfo> labelInfoList;

    public EmergencyView(Context context, Bitmap bitmap, List<ImageLabelInfo> labelInfoList) {
        super(context);
        this.bitmap = bitmap;
        this.labelInfoList = labelInfoList;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    public EmergencyView(Context context, @Nullable AttributeSet attrs, Bitmap bitmap, List<ImageLabelInfo> labelInfoList) {
        super(context, attrs);
        this.bitmap = bitmap;
        this.labelInfoList = labelInfoList;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.MAGENTA);
    }

    public EmergencyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, Bitmap bitmap, List<ImageLabelInfo> labelInfoList) {
        super(context, attrs, defStyleAttr);
        this.bitmap = bitmap;
        this.labelInfoList = labelInfoList;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.MAGENTA);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setBitmap(bitmap);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.MAGENTA);

        int startWidth, stopWidth;
        int startHeight, stopHeight;

        for(int i=0; i<4; i++){
            startWidth = (bitmap.getWidth() / 5) * (i+1);
            startHeight = 0;
            stopWidth = (bitmap.getWidth() / 5) * (i+1);
            stopHeight = bitmap.getHeight();
            canvas.drawLine(startWidth,startHeight,stopWidth,stopHeight, mPaint);
        }

        for(int i=0; i<4; i++){
            startWidth = 0;
            startHeight = (bitmap.getHeight() / 5) * (i+1);
            stopWidth = bitmap.getWidth();
            stopHeight = (bitmap.getHeight() / 5) * (i+1);
            canvas.drawLine(startWidth, startHeight, stopWidth, stopHeight, mPaint);
        }

        mPaint.setColor(Color.YELLOW);
        mPaint.setStyle(Paint.Style.FILL);

        for(int i=0; i<25; i++){

            int safeIndex=0;
            int startX, startY;
            int stopX, stopY;

            for(ImageLabelInfo labelInfo : labelInfoList){
                if(labelInfo.getKey().equals("safe")){
                    safeIndex = labelInfoList.indexOf(labelInfo);
                }
            }

            startX = (safeIndex % 5) * (canvas.getWidth()/5);
            startY = (safeIndex / 5) * (canvas.getHeight()/5);
            stopX = startX + (canvas.getWidth()/5);
            stopY = startY + (canvas.getHeight()/5);

            canvas.drawRect(startX, startY, stopX, stopY, mPaint);
        }
    }

}
