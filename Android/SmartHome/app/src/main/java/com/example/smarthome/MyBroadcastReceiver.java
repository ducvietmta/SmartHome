package com.example.smarthome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.LinkedList;
import java.util.Queue;

public class MyBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_FIRST_ACTION = "Send_data_BroadcastIntent";
    int data;
    MyBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(MyBroadcastReceiver.ACTION_FIRST_ACTION)) {
                data = intent.getIntExtra("data", 0);
        }
    }
}
