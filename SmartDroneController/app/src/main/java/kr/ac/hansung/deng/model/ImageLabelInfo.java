package kr.ac.hansung.deng.model;

import android.graphics.Bitmap;

public class ImageLabelInfo {
    private String key;
    private float value;
    private int row;
    private int cols;
    private Bitmap image;

    public ImageLabelInfo(String key, int row, int cols) {
        this.key = key;
        this.row = row;
        this.cols = cols;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public Bitmap getImage(){return image;}
}
