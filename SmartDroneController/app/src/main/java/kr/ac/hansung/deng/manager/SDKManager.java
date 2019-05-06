package kr.ac.hansung.deng.manager;

import android.os.Handler;
import android.view.TextureView;

public interface SDKManager {
    // connection
    void connect();

    // drone's function
    void getVideo(TextureView textureView);
    void removeVideo();
    void getCapture(TextureView textureView);
    void moveGimbalDown();
    void moveGimbalUp();

    // left joystick
    void turnLeft(float xPosition, float yPosition);
    void turnRight(float xPosition, float yPosition);
    void up(float xPosition, float yPosition);
    void down(float xPosition, float yPosition);

    //right joystick
    void right(float xPosition, float yPosition);
    void left(float xPosition, float yPosition);
    void forward(float xPosition, float yPosition);
    void back(float xPosition, float yPosition);

    void takeOff();
    void landing();
}
