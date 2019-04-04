package kr.ac.hansung.deng.manager;

public interface SDKManager {
    // connection
    void connect();

    // drone's function
    void getVideo();
    void getCapture();

    // left joystick
    void turnLeft();
    void turnRight();
    void up();
    void down();

    //right joystick
    void right();
    void left();
    void forward();
    void back();

    void takeOffDrone();
    void landingDrone();
}
