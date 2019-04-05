package kr.ac.hansung.deng.data;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;


/**
 * Image data process
 *
 * �어��지륕식�라 bitmap�로 변경시켜주class
 *
 * bytes , filePath , stream�로 �어�는 경우
 * overloading �어 �는 process 메소�� �해 convert
 */

public class ImgDataProcess{
    private Bitmap capturedImage;
    //byte �이�로 �어�는 경우
    public void process(byte [] bytes) {
        capturedImage = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
    //file�가�오경우
    public void process(String path){
        capturedImage = BitmapFactory.decodeFile(path);
    }
    //stream�해 �이�� �어�는 경우
    public void process(InputStream is){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        capturedImage = BitmapFactory.decodeStream(is,null,options);
    }

    public Bitmap getCapturedImage() {
        return capturedImage;
    }

}
