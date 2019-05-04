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

    // left joystick
    void leftJoystickAction(float xPosition, float yPosition);

    //right joystick
    void rightJoystickAction(float xPosition, float yPosition);

    void takeOff();
    void landing();
}
