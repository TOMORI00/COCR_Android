package com.nju.cocr.structure;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.nju.cocr.dnn.Detector;
import com.nju.cocr.dnn.DetectorInterface;
import com.nju.cocr.dnn.Recognition;
import com.nju.cocr.dnn.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class endPointDetectorTest {
    DetectorInterface detector;
    List<List<PointF>> testCase_script;// 测试用例的笔迹
    Context ctx;

    @Before
    public void setUp() throws Exception {
        detector = Detector.getInstance();
        detector.setThreadNum(4);
        detector.enableNNAPI(true);
        detector.setConfThresh(0.3f);
        detector.setIOUThresh(0.3f);
        ctx = ApplicationProvider.getApplicationContext();
        detector.initialize(ctx.getAssets());
        testCase_script = readTestCase("testcase/testcase1");
    }

    /**
     * @param path 文件路径
     * @return 测试用例的script数据
     */
    private List<List<PointF>> readTestCase(String path) {
        //返回结果
        List<List<PointF>> result = new ArrayList<>();
        try {
            //文件读取
            InputStream testcase1 = ctx.getAssets().open(path);
            InputStreamReader read = new InputStreamReader(testcase1);
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = bufferedReader.readLine();
            //行遍历
            while (lineTxt != null) {
                //处理冗余字符
                lineTxt = lineTxt.substring(8, lineTxt.length() - 3);
                String[] temp = lineTxt.split("\\), PointF\\(");
                //原文件中一行的数据
                List<PointF> lineList = new ArrayList<>();
                for (String s : temp) {
                    String[] PointF_string = s.split(", ");
                    float x = Float.parseFloat(PointF_string[0]);
                    float y = Float.parseFloat(PointF_string[1]);
                    lineList.add(new PointF(x, y));
                }
                //行并入总结果
                result.add(lineList);
                lineTxt = bufferedReader.readLine();
            }
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Test
    public void insertDotTest() {
        String TAG = "insertDotTest";
        PointF startPoint = new PointF(1, 1);
        PointF endPoint = new PointF(15, 15);
        List<List<PointF>> paths = new ArrayList<>();
        List<PointF> path = new ArrayList<>();
        path.add(startPoint);
        path.add(endPoint);
        paths.add(path);
        endpointDetector e = new endpointDetector(paths, null);
        List<PointF> result = e.insertDots();
        Log.d(TAG, "insertDotTest: " + result);
        assertEquals(11, result.size());
    }

    @Test
    public void detectTest() {
        String TAG = "detectTest";
        Bitmap image = Utils.convertScriptToBitmap(Utils.normalizeScript(testCase_script));
        List<Recognition> objects = detector.getRecognition(image);
        //保存框
        List<RectF> labels = new ArrayList<>();
        for (Recognition recognition : objects) {
            //<=4的时候说明框里面是键
            if (recognition.getIndex() <= 4) {
                labels.add(recognition.getBoundingBox());
            }
        }
        endpointDetector e = new endpointDetector(testCase_script, labels);
        //result为方向
        List<Direction> result = e.detect();
        Log.d(TAG, String.valueOf(objects));
        Log.d(TAG, String.valueOf(labels));
        Log.d(TAG, String.valueOf(result));
    }

    @After
    public void tearDown() throws Exception {
        detector.close();
    }
}
