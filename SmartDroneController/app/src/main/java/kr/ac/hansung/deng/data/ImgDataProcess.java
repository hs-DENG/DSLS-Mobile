package kr.ac.hansung.deng.data;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

import kr.ac.hansung.deng.smartdronecontroller.R;

public class ImgDataProcess implements DataProcessor {

    @Override
    public void process() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        //Resources res= getResources();


        //Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.icon);

        //bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);


        byte[] image = outStream.toByteArray();
        String profileImageBase64 = Base64.encodeToString(image, 0);
    }
}
