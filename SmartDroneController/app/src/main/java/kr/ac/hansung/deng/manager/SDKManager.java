package kr.ac.hansung.deng.manager;

public interface SDKManager {
    // connection
    void connect();

    // drone's function
    void getVideo();
    void getCapture();

    // left joystick
    void leftJoystickAction(float xPosition, float yPosition);

    //right joystick
    void rightJoystickAction(float xPosition, float yPosition);

    void takeOff();
    void landing();
}
