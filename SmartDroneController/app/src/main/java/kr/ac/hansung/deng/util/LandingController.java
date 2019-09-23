package kr.ac.hansung.deng.util;

import kr.ac.hansung.deng.manager.SDKManager;
import kr.ac.hansung.deng.model.ImageLabelInfo;

public class LandingController extends Thread{

    private SDKManager sdkManager;
    private int height;
    private ImageLabelInfo labelInfo;

    @Override
    public void run() {
        smartLanding();
    }

    private void smartLanding(){
        int centerRow = height/2;
        int centerCols = height/2;

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

    public SDKManager getSdkManager() {
        return sdkManager;
    }

    public void setSdkManager(SDKManager sdkManager) {
        this.sdkManager = sdkManager;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ImageLabelInfo getLabelInfo() {
        return labelInfo;
    }

    public void setLabelInfo(ImageLabelInfo labelInfo) {
        this.labelInfo = labelInfo;
    }
}
