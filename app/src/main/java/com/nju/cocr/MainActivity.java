package com.nju.cocr;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
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
import com.nju.cocr.view.ScribbleView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";
    ScribbleView scribbleView;
    FloatingActionButton clsButton, ocrButton, exchangeButton, eraserButton, penButton, saveButton;
    FloatingActionsMenu menuButton;
    Context ctx;
    DetectorInterface detector;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scribbleView = findViewById(R.id.scribble_view);
        menuButton = findViewById(R.id.menu_button);
        clsButton = findViewById(R.id.cls_button);
        ocrButton = findViewById(R.id.ocr_button);
        exchangeButton = findViewById(R.id.exchange_button);
        eraserButton = findViewById(R.id.eraser_button);
        penButton = findViewById(R.id.pen_button);
        saveButton = findViewById(R.id.save_button);

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
                scribbleView.setDrawingstate(0);
                Toast toast = Toast.makeText(getBaseContext(), R.string.scribble_ocr, Toast.LENGTH_SHORT);
                toast.show();
                // runForBitmap("testcase/1a0h.jpg", 28, false);
                // runForBitmap("testcase/234d.jpg", 51, false);
                List<Recognition> objects = detector.getRecognition(scribbleView.getScript());
                Log.d(TAG,"XGD:"+scribbleView.getScript());
                Log.d(TAG,"XGD:"+objects.toString());
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
}