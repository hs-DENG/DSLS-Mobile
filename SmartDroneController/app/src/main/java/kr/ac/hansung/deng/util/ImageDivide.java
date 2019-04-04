package kr.ac.hansung.deng.util;

import android.graphics.Bitmap;
import java.util.ArrayList;
import java.util.List;

/**
 *  class ImageDivde
 *
 *  생성할 때 비트맵 이미지와 분할할 크기로 인자값을 넘겨 받아야한다.
 *  - cropImage() 메소드로 분할하면됨.
 *  - getCroppedImages()로 분할된 이미지가 들어있는 List<E>를 가져오면 됨.
 */

public class ImageDivide {
    private Bitmap capturedImage = null; //넘겨받을 캡쳐이미지
    private List<Bitmap> croppedImages;//분할된 사진들이 들어있는 벡터
    private int size;//분할하고자 하는 크기

    /**
     @param capturedImage : 비트맵 이미지
     @param size : 분할할 크기
     */
    public ImageDivide(Bitmap capturedImage,int size){
        croppedImages = new ArrayList<Bitmap>();
        this.capturedImage = capturedImage;
        this.size = size;
    }

    public Bitmap getCapturedImage() {
        return capturedImage;
    }

    public void setCapturedImage(Bitmap capturedImage) {
        this.capturedImage = capturedImage;
    }

    //분할된 이미지 가져오기
    public List<Bitmap> getCroppedImages() {
        return croppedImages;
    }

    //이미지 분할
    public void cropImage(){
        if(capturedImage==null){
            return;
        }

        int width = capturedImage.getWidth();
        int height = capturedImage.getHeight();

        for(int i=0;i<size;i++){

            for(int j=0;j<size;j++){
                Bitmap croppedImage;

                if(i==0&&j==0){
                    croppedImage = Bitmap.createBitmap(capturedImage,0,0,width/size,height/size);
                }
                else if(i==0){
                    croppedImage = Bitmap.createBitmap(capturedImage,j*width/size,0,width/size,height/size);
                }
                else if(j==0){
                    croppedImage = Bitmap.createBitmap(capturedImage,0,i*height/size,width/size,height/size);
                }
                else{
                    croppedImage = Bitmap.createBitmap(capturedImage,j*width/size,i*height/size,width/size,height/size);
                }
                croppedImages.add(croppedImage);
            }
        }
    }
}
