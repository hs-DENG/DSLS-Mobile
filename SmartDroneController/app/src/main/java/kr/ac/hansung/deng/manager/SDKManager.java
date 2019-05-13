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
//    void leftJoystickAction(float xPosition, float yPosition);
    void up();
    void down();
    void turnLeft();
    void turnRight();

    //right joystick
 //   void rightJoystickAction(float xPosition, float yPosition);
    void forward();
    void back();
    void left();
    void right();

    void takeOff();
    void landing();
}
