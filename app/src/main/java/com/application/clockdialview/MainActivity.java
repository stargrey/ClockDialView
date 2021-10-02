package com.application.clockdialview;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        findViewById(R.id.btn_getTime).setOnClickListener(view -> {
            ClockDialView clockDialView = findViewById(R.id.clock_dial_view);
            int[] time = clockDialView.getTime();
            Toast.makeText(getBaseContext(),"Time is " + time[0] + ":" + time[1], Toast.LENGTH_SHORT).show();
        });
    }
}