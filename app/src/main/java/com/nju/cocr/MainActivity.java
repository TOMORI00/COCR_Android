package com.nju.cocr;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.nju.cocr.view.ScribbleView;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";
    ScribbleView scribbleView;
    FloatingActionButton clsButton, ocrButton, exchangeButton;
    FloatingActionsMenu menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
}