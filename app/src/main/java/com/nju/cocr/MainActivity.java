package com.nju.cocr;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.nju.cocr.dnn.Detector;
import com.nju.cocr.dnn.DetectorInterface;
import com.nju.cocr.dnn.Recognition;
import com.nju.cocr.dnn.Utils;
import com.nju.cocr.structure.Atom;
import com.nju.cocr.structure.Bond;
import com.nju.cocr.structure.Direction;
import com.nju.cocr.structure.Synthesizer;
import com.nju.cocr.structure.endpointDetector;
import com.nju.cocr.view.ScribbleView;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";
    ScribbleView scribbleView;

    FloatingActionButton clsButton, ocrButton, exchangeButton, eraserButton, penButton, saveButton;
    FloatingActionsMenu menuButton;
    Context ctx;
    DetectorInterface detector;

    List<List<PointF>> testCase_script;// 测试用例的笔迹
    List<Direction> directions;
    List<Recognition> recognitions;

    String[] needPermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    void runForBitmap(final String filename, int trueNum, boolean showDetails) {
        Bitmap image = Utils.readBitmapFromAssets(ctx.getAssets(), filename);
        long start = System.currentTimeMillis();
        List<Recognition> objects = detector.getRecognition(image);
        long end = System.currentTimeMillis();
        Log.d(TAG, "cost " + (end - start) + "ms");
        if (showDetails) {
            Log.d(TAG, "find " + objects.size() + " items:" + objects);
        }
        image.recycle();
    }

    Synthesizer synthesizer;

    //public String a = "asdasddsas";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scribbleView = findViewById(R.id.scribble_view);

        menuButton = findViewById(R.id.menu_button);
        clsButton = findViewById(R.id.cls_button);
        ocrButton = findViewById(R.id.ocr_button);
        //drawQuadToView = findViewById(R.id.drawQuadTo_view);
        exchangeButton = findViewById(R.id.exchange_button);
        eraserButton = findViewById(R.id.eraser_button);
        penButton = findViewById(R.id.pen_button);
        saveButton = findViewById(R.id.save_button);
        //lineButton = findViewById(R.id.line_button);

        ctx=getBaseContext();
        detector = Detector.getInstance();
        detector.setThreadNum(4);
        detector.enableNNAPI(true);
        detector.setConfThresh(0.3f);
        detector.setIOUThresh(0.3f);
        detector.initialize(ctx.getAssets());




        clsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scribbleView.setDrawingstate(0);
                scribbleView.clear();
                scribbleView.invalidate();
                Toast toast = Toast.makeText(getBaseContext(), R.string.scribble_cls, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        ocrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scribbleView.setDrawingstate(2);
                //testCase_script = readTestCase(); //todo 改成实际的scripts 不改也没事
                testCase_script = readTestCase("testcase/testcase1");
                // 这一步是需要的，要把画的调入内存给模型用
                Bitmap image = Utils.convertScriptToBitmap(Utils.normalizeScript(testCase_script));
                //recognitions是模型的产物
                recognitions = detector.getRecognition(image);
                //保存框
                List<RectF> labels = new ArrayList<>();
                for (Recognition recognition : recognitions) {
                    //<=4的时候说明框里面是键
                    if (recognition.getIndex() <= 4) {
                        labels.add(recognition.getBoundingBox());
                    }
                }
                //直线端点判断
                endpointDetector e = new endpointDetector(testCase_script, labels);
                //result为方向
                directions = e.detect();
                Synthesizer synthesizer = new Synthesizer(recognitions, directions);
                synthesizer.checkExplicitAtom();
                synthesizer.findHiddenCarbon();
                synthesizer.checkExplicitAtom();
                synthesizer.addTerminalCarbon();
                synthesizer.checkExplicitAtom();

                //这些是调用接口
                List<Atom> atoms = synthesizer.getAtoms();
                List<Bond> bonds = synthesizer.getBonds();
                for (Atom atom : atoms) {
                    Log.d(TAG, atom.toString());
                    scribbleView.atoms1.add(atom);
                }
                for (Bond bond : bonds) {
                    Log.d(TAG, bond.toString());
                    scribbleView.bonds1.add(bond);
                }
                Log.d(TAG, "__________");
                for (Atom atom : scribbleView.atoms1) {
                   Log.d(TAG, atom.toString());
                }
                for (Bond bond : scribbleView.bonds1) {
                    Log.d(TAG, bond.toString());
                }
                Log.d(TAG, "onClick: "+scribbleView.getDrawingstate());

                Toast toast = Toast.makeText(getBaseContext(), R.string.scribble_ocr, Toast.LENGTH_SHORT);
                toast.show();
                //runForBitmap("testcase/1a0h.jpg", 28, false);
                // runForBitmap("testcase/234d.jpg", 51, false);
                //List<Recognition> objects = detector.getRecognition(scribbleView.getScript());
                //Log.d(TAG,"XGD:"+scribbleView.getScript());
                //Log.d(TAG,"XGD:"+objects.toString());
            }
        });
        exchangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scribbleView.setDrawingstate(0);
                Toast toast = Toast.makeText(getBaseContext(), R.string.scribble_exchange, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        eraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scribbleView.setDrawingstate(1);
                Toast toast = Toast.makeText(getBaseContext(), R.string.scribble_exchange, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        penButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scribbleView.setDrawingstate(0);
                Toast toast = Toast.makeText(getBaseContext(), R.string.scribble_exchange, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT<29 && ContextCompat.checkSelfPermission(MainActivity.this
                        ,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    requestPermissions(needPermission, 1);
                }else {
                    savePhoto();
                }
            }

        });
    }


    private void savePhoto(){
        Bitmap bitmap = scribbleView.viewConversionBitmap(scribbleView);
        if(MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"","")==null){
            Toast.makeText(MainActivity.this,"fail",Toast.LENGTH_SHORT).show();
        }else {
        Toast.makeText(MainActivity.this,"success",Toast.LENGTH_SHORT).show();
    }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

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
}