package com.example.smarthome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class Main6Activity extends AppCompatActivity {
    Button mBtnLight,mBtnDoor;
    int onLightClick = 0, onFanClick = 0, onWindowClick = 0, onDoorClick = 0;
    int data  = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);
        transparentStatusAndNavigation();
        mBtnLight = findViewById(R.id.btnLightGR);
        mBtnDoor = findViewById(R.id.btnDoorGR);
        String actionName = "Send_data_BroadcastIntent";
        final Intent intent = new Intent(actionName);
        intent.setAction(actionName);
        mBtnLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onLightClick ==1) {
                    v.setBackgroundResource(R.drawable.light_off);
                    intent.putExtra("data", 22);
                    sendBroadcast(intent);
                    onLightClick = 0;
                }else{
                    v.setBackgroundResource(R.drawable.light_on);
                    intent.putExtra("data", 23);
                    sendBroadcast(intent);
                    onLightClick ++;
                }
            }
        });
        mBtnDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onDoorClick ==1) {
                    v.setBackgroundResource(R.drawable.window);
                    intent.putExtra("data", 24);
                    sendBroadcast(intent);
                    onDoorClick = 0;
                }else{
                    v.setBackgroundResource(R.drawable.window_on);
                    intent.putExtra("data", 25);
                    sendBroadcast(intent);
                    onDoorClick ++;
                }
            }
        });

    }
    private void transparentStatusAndNavigation() {
        //make full transparent statusBar
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
    }
    private void setWindowFlag(final int bits, boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
}