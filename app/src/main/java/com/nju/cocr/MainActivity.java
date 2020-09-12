package com.nju.cocr;

import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.nju.cocr.view.ScribbleView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";
    ScribbleView scribbleView;
    FloatingActionButton clsButton, ocrButton, exchangeButton;
    FloatingActionsMenu menuButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scribbleView = findViewById(R.id.scribble_view);
        menuButton = findViewById(R.id.menu_button);
        clsButton = findViewById(R.id.cls_button);
        ocrButton = findViewById(R.id.ocr_button);
        exchangeButton = findViewById(R.id.exchange_button);
        clsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scribbleView.clear();
                scribbleView.invalidate();
                Toast toast = Toast.makeText(getBaseContext(), R.string.scribble_cls, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        ocrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast = Toast.makeText(getBaseContext(), R.string.scribble_ocr, Toast.LENGTH_SHORT);
                toast.show();
                List<List<PointF>> script = scribbleView.getScript();
                for (int i = 0; i < script.size(); i++) {
                    for (int j = 0; j < script.get(i).size(); j++) {
                        Log.d(TAG, String.valueOf(script.get(i).get(j)));
                    }
                    Log.d(TAG, "==========================================");
                }
            }
        });
        exchangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast = Toast.makeText(getBaseContext(), R.string.scribble_exchange, Toast.LENGTH_SHORT);
                toast.show();
            }
        });


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