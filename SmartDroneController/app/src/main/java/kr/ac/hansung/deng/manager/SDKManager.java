package kr.ac.hansung.deng.manager;

public interface SDKManager {
    // connection
    void connect();

    // drone's function
    void getVideo();
    void getCapture();

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
