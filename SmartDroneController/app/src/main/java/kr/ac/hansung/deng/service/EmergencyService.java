package kr.ac.hansung.deng.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Binder;
import android.os.IBinder;
import android.text.SpannableStringBuilder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import kr.ac.hansung.deng.ML.ImageClassifier;
import kr.ac.hansung.deng.ML.ImageClassifierFloatInception;
import kr.ac.hansung.deng.ML.SafeImageClassifier;
import kr.ac.hansung.deng.activity.MainActivity;
import kr.ac.hansung.deng.driver.DJISDKDriver;
import kr.ac.hansung.deng.manager.EmergencyLandingManager;
import kr.ac.hansung.deng.manager.SDKManager;
import kr.ac.hansung.deng.model.ImageLabelInfo;
import kr.ac.hansung.deng.util.ImageDivide;
import kr.ac.hansung.deng.util.LandingController;
import kr.ac.hansung.deng.util.ResultDrawer;

public class EmergencyService extends Service {

    private static final String TAG = EmergencyService.class.getSimpleName();

    // service thread
    private Thread mThread = null;

    // for drone control
    private MainActivity mainActivity;
    private SDKManager sdkManager;

    // reference that for run learning model
    private Bitmap testData;
    private List<Bitmap> divededImages;
    private List<Bitmap> processedImages;
    private ImageClassifier classifier , safeClassifier;

    // tool for drawing divided section of safe/unsafe information
    private Canvas canvas;
    private final static int line = Color.BLACK;
    private final static int safeArea = Color.GREEN;
    private final static int unsafeArea = Color.RED;

    // model
    private List<ImageLabelInfo> labelInfoList = new ArrayList<ImageLabelInfo>();
    public EmergencyService() {
        sdkManager = DJISDKDriver.getInstance();
    }
    private SpannableStringBuilder textToShow;

    //
    private Graph graph;
    private float height=0;
    private LandingController landingController = new LandingController();
    private boolean landing = false;
    private ResultDrawer resultDrawer;

    private String landingAreaText = "";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        EmergencyLandingManager.getInstance().setRunning(true);
        if(mThread == null){
            mThread = new Thread("My Thread"){
                @Override
                public void run(){
                    try {
                        processedImages = new ArrayList<Bitmap>();

                        while (true) {
                            if (height < 5) break;
                            height = sdkManager.getAircraftHeight(); // �이 가�오�
                            sleep(2000);
                            sdkManager.down();
                            sleep(2000);
                        }

                        height=5;

                        graph = new Graph((int)(height*height));

                        // 카메짐볼 �리�
                        ((DJISDKDriver) sdkManager).moveGimbalDownAll();
                        sleep(5000);
                        Log.d(TAG,"camera gimbal down all");

                        //캡처
                        sdkManager.getCapture(mainActivity.getmVideoSurface());
                        testData = ((DJISDKDriver) sdkManager).getCaptureView();
                        Log.d(TAG,"using camera capture function for get area data");

                        ImageDivide divide = new ImageDivide(testData, (int) height); // ��지 divide �이 만큼 divide

                        divide.cropImage(); // divide �행

                        divededImages = divide.getCroppedImages(); // divide 결과 리스가�오�

                        Log.d(TAG,"area data divide");

                        for (Bitmap image : divededImages) {
                            processedImages.add(Bitmap.createScaledBitmap(image, 299,299, true)); // resize
                        }

                        for(int i=0; i < height*height ; i++){
                            if(i > 0 && i % height != 0 )
                                graph.addEdge(i, i - 1);

                            if(i < height*height && i % height != height -1 )
                                graph.addEdge(i, i+1);

                            if(i-height > 0)
                                graph.addEdge(i, (int)(i-height));

                            if(i+height < height*height)
                                graph.addEdge(i, (int)(i+height));
                        }


                        // 너비우선 탐색을 이용 하여 스마트 랜딩
                        graph.runBFS((int)((height*height)/2),classifier, processedImages);



                        // safe area classfication
                        safeClassifier = new SafeImageClassifier(mainActivity);
                        safeClassifier.setNumThreads(1);
                        SpannableStringBuilder textToShow1 = new SpannableStringBuilder();
                        Log.d(TAG,"img : " + graph.getLandingImg());

                        // TODO graph.getLandingImg() 가 null 일때 예외처리

                        if(graph.getLandingImg() == null){
                            safeClassifier.classifyFrame(labelInfoList.get(0).getImage(), textToShow1);
                            landingAreaText = "safe area not found";
                            Log.d(TAG, "label : " + landingAreaText);
                        }
                        else{
                            safeClassifier.classifyFrame(graph.getLandingImg(), textToShow1);
                            landingAreaText = safeClassifier.getLabelProcess().getLabelList().get(0).getKey();
                            Log.d(TAG, "label : " + landingAreaText);
                        }

                        //draw section
                        resultDrawer = new ResultDrawer();
                        resultDrawer.drawAreaSection(mainActivity, (int) height, testData, labelInfoList, landingAreaText);

                        // Release Resources

                        testData.recycle();

                        for (Bitmap image : divededImages) {
                            image.recycle();
                        }
                        for(Bitmap image: processedImages){
                            image.recycle();
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                        Log.e(TAG,e.getMessage());
                    }
                }
            };
            mThread.start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EmergencyLandingManager.getInstance().setRunning(false);
        Log.d(TAG, "onDestroy()");

        if(mThread != null)
            mThread = null;
    }

    public class MyBinder extends Binder {
        public EmergencyService getService(){
            return EmergencyService.this;
        }
    }

    private IBinder mBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    public void setActivity(MainActivity activity){
        this.mainActivity = activity;
    }

    public ImageClassifier getClassifier() {
        return classifier;
    }

    class Graph {
        /* 인접 리스트를 이용한 방향성 있는 그래프 클래스 */

        private int V; // 노드의 개수
        private LinkedList<Integer> adj[]; // 인접 리스트
        private Bitmap landingImg, nullImg;

        /** 생성자 */
        public Graph(int v) {
            V = v;
            adj = new LinkedList[v];
            for (int i=0; i<v; ++i) // 인접 리스트 초기화
                adj[i] = new LinkedList();
        }

        /** 노드를 연결 v->w */
        public void addEdge(int v, int w) { adj[v].add(w); }

        public void addEdgeBidirection(int v, int w){
            adj[v].add(w);
            adj[w].add(v);
        }

        /** s를 시작 노드으로 한 BFS로 탐색하면서 탐색한 노드들을 출력 */

        public void runBFS(int edge, ImageClassifier classifier, List<Bitmap> processedImg){
            boolean visited[] = new boolean[V];
            // BFS 구현을 위한 큐(Queue) 생성
            LinkedList<Integer> queue = new LinkedList<Integer>();

            // 현재 노드를 방문한 것으로 표시하고 큐에 삽입(enqueue)
            visited[edge] = true;

            queue.add(edge);

            // 큐(Queue)가 빌 때까지 반복
            while (queue.size() != 0) {
                // 방문한 노드를 큐에서 추출(dequeue)하고 값을 출력
                edge = queue.poll();
                try {
                    classifier = new ImageClassifierFloatInception(mainActivity);
                    classifier.setNumThreads(1);
                    textToShow = new SpannableStringBuilder();
                }catch (Exception e){
                    e.printStackTrace();
                }

                classifier.classifyFrame(processedImages.get(edge), textToShow);

                ImageLabelInfo label = new ImageLabelInfo(classifier.getLabelProcess().getLabelList().get(0).getKey(),(int)(edge/height),(int)(edge%height));

                label.setImage(processedImages.get(edge));

                Log.d(TAG,"row : " + label.getRow() + ", cols : " + label.getCols() + ", value : " + label.getKey() + ", edge : " + edge);

                if(label.getKey().equals("safe") && landing == false){

                    landingImg = label.getImage();

                    landingController.setSdkManager(sdkManager);

                    landingController.setHeight((int)height);
                    landingController.setLabelInfo(label);

                    landingController.run();

                    landing = true;
                }


                classifier.close();
                labelInfoList.add(label);

                // 방문한 노드와 인접한 모든 노드를 가져온다.
                Iterator<Integer> i = adj[edge].listIterator();
                while (i.hasNext()) {
                    int n = i.next();
                    // 방문하지 않은 노드면 방문한 것으로 표시하고 큐에 삽입(enqueue)
                    if (!visited[n]) {

                        visited[n] = true;

                        queue.add(n);
                    }
                }

            }
            landing = false;
        }

        public Bitmap getLandingImg() {
            return landingImg;
        }

    }
}