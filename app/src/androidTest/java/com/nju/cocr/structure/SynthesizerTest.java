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


public class SynthesizerTest {
    DetectorInterface detector;
    List<List<PointF>> testCase_script;// 测试用例的笔迹
    Context ctx;
    String TAG = "detectTest";
    List<Direction> directions;
    List<Recognition> recognitions;

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
        Bitmap image = Utils.convertScriptToBitmap(Utils.normalizeScript(testCase_script));
        recognitions = detector.getRecognition(image);
        //保存框
        List<RectF> labels = new ArrayList<>();
        for (Recognition recognition : recognitions) {
            //<=4的时候说明框里面是键
            if (recognition.getIndex() <= 4) {
                labels.add(recognition.getBoundingBox());
            }
        }
        endpointDetector e = new endpointDetector(testCase_script, labels);
        //result为方向
        directions = e.detect();
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
    public void findHiddenCarbonTest() {
        Synthesizer synthesizer = new Synthesizer(recognitions, directions);
        List<Atom> atoms = synthesizer.getAtoms();
        List<Bond> bonds = synthesizer.getBonds();
        List<List<Atom>> cons = synthesizer.getCons();
        for (List<Atom> con : cons) {
            StringBuilder line = new StringBuilder();
            for (Atom atom : con) {
                line.append(atom.toString());
            }
            Log.d(TAG, line.toString());
        }
        for (Atom atom : atoms) {
            Log.d(TAG, atom.toString());
        }
        for (Bond bond : bonds) {
            Log.d(TAG, bond.toString());
        }
        synthesizer.findHiddenCarbon();
        Log.d(TAG, "================================");
        for (Atom atom : atoms) {
            Log.d(TAG, atom.toString());
        }
        for (Bond bond : bonds) {
            Log.d(TAG, bond.toString());
        }


    }

    @Test
    public void checkExplicitAtomTest() {
        Synthesizer synthesizer = new Synthesizer(recognitions, directions);
        synthesizer.checkExplicitAtom();
        List<Atom> atoms = synthesizer.getAtoms();
        List<Bond> bonds = synthesizer.getBonds();
        List<List<Atom>> cons = synthesizer.getCons();
        for (Atom atom : atoms) {
            Log.d(TAG, atom.toString());
        }
        for (Bond bond : bonds) {
            Log.d(TAG, bond.toString());
        }
        for (List<Atom> con : cons) {
            Log.d(TAG, con.toString());
        }

    }

    @After
    public void tearDown() throws Exception {
        detector.close();
    }
}
