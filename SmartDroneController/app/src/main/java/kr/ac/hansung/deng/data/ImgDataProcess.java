package kr.ac.hansung.deng.data;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import kr.ac.hansung.deng.activity.EmergencyActivity;
import kr.ac.hansung.deng.app.MainActivity;
import kr.ac.hansung.deng.smartdronecontroller.R;

/**
 * Image data process
 *
 * 넘어온 이미지를 형식에 따라 bitmap으로 변경시켜주는 class
 *
 * bytes , filePath , stream으로 넘어오는 경우에
 * overloading 되어 있는 process 메소드를 통해 convert
 */

public class ImgDataProcess{

    private Bitmap capturedImage;

    //byte 데이터로 넘어오는 경우
    public void process(byte [] bytes) {
        capturedImage = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

    //file로 가져오는 경우
    public void process(String path){

        capturedImage = BitmapFactory.decodeFile(path);
    }

    //stream을 통해 데이터가 넘어오는 경우
    public void process(InputStream is){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        capturedImage = BitmapFactory.decodeStream(is,null,options);
    }

    public Bitmap getCapturedImage() {
        return capturedImage;
    }

}
