package kr.ac.hansung.deng.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import kr.ac.hansung.deng.ML.ImageClassifier;


/**
 * Class LabelProcess
 *
 * ImageClassifier Class에서 모델을 실행시켰을 시에
 * 해당 이미지에 대한 라벨 ( name + 값 )을 labelList에 저장
 *
 * 시용법
 * 생성 : 생성자에 ImageClassifier 인스턴스 전달
 * setLabelList() : ImageClassifier에 있는 sortedLabels(PriorityQueue)를 넘겨주어야함
 */

public class LabelProcess {
    private final String TAG = "LabelProcess";
    private List<Map.Entry<String,Float>> labelList;//TODO row, cols
    private ImageClassifier mClassifier;

    public LabelProcess(ImageClassifier classifier){
        this.mClassifier = classifier;
    }

    //safe, unsafe중 값이 더 큰 라벨을 저장
    //PriorityQueue를 array로 변환 후 처리
    public void setLabelList(PriorityQueue<Map.Entry<String, Float>> sortedLabels)
    {
        Object [] sortedArray = sortedLabels.toArray();
        Map.Entry<String,Float> maxLabel = (Map.Entry<String,Float>)sortedArray[0];
        int size = sortedArray.length;

        for(int i=0;i<size;i++){
            if(maxLabel.getValue()<((Map.Entry<String,Float>)sortedArray[i]).getValue()){
                maxLabel = (Map.Entry<String,Float>)sortedArray[i];
            }
        }
        labelList = new ArrayList<Map.Entry<String, Float>>();
        labelList.add(maxLabel);
    }
    public List<Map.Entry<String,Float>> getLabelList(){
        return labelList;
    }
}