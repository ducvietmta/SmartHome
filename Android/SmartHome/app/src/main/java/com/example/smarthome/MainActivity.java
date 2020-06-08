package com.example.smarthome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    Button btSpeech;
    TextView txtSpeechInput;
    ListView listRoom;
    Button btBluetooth;
    Boolean onLongClick = false;
    ListView lvRoom;
    ArrayList <roomClass> arrayRoom;
    roomAdapter adapter;
    private Thread thread;
    MyBroadcastReceiver myBroadcastReceiver;
    int old_data;
    private final String TAG = MainActivity.class.getSimpleName();

    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    public final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;

    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        transparentStatusAndNavigation();
        final Animation animScan = AnimationUtils.loadAnimation(this, R.anim.bluetooth_scan_anim);
        btSpeech = findViewById(R.id.btSpeech);
        txtSpeechInput = findViewById(R.id.txtSpeechInput);
        btBluetooth = findViewById(R.id.btBluetooth);
        listRoom = findViewById(R.id.lvRoom);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        anhxa();
        initBroadcastReceiver();
        updateData();
        NotificationManager notif=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        adapter = new roomAdapter(this, R.layout.listview_layout, arrayRoom);
        lvRoom.setAdapter(adapter);
        btSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
        btBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBTAdapter.isEnabled()) {
                    Toast.makeText(MainActivity.this.getApplicationContext(), "Turning On Bluetooth ...", Toast.LENGTH_SHORT).show();
                    MainActivity.this.checkPermissions(mBTAdapter);
                } else {
                    if (!onLongClick) {
                        v.setBackgroundResource(R.drawable.ic_bluetooth_off);
                        v.startAnimation(animScan);
                        MainActivity.this.bluetoothOn();
                        connectDevice();
                    }
                    onLongClick = false;

                }
            }
        });
        btBluetooth.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onLongClick = true;
                v.setBackgroundResource(R.drawable.ic_bluetooth_off);
                bluetoothOff();
                return false;
            }
        });
        listRoom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object listItem = listRoom.getItemAtPosition(position);
                switch ( position ) {
                    case 0:
                        openActivity2();
                        break;
                    case 1:
                        openActivity3();
                        break;
                    case 2:
                        openActivity4();
                        break;
                    case 3:
                        openActivity5();
                        break;
                    case 4:
                        openActivity6();
                        break;
                    default:
                        // Làm gì đó tại đây ...
                }
            }
        });
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == MESSAGE_READ){
                    String noti = "";
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    if(readMessage.substring(0,4).equals("0001") == true) {
                        noti = "Cửa đã được mở an toàn";
                        Toast.makeText(getApplicationContext(),noti,Toast.LENGTH_LONG).show();
                    }
                    if(readMessage.substring(0,4).equals("0002")== true) {
                        noti = "Có thiết bị truy cập trái phép";
                        Toast.makeText(getApplicationContext(),noti,Toast.LENGTH_LONG).show();
                    }
                    if(readMessage.substring(0,4).equals("0003")== true) {
                        noti = "Cảnh báo dò khí GAS";
                        Toast.makeText(getApplicationContext(),noti,Toast.LENGTH_LONG).show();
                    }
                    sendNotification(noti);

                }

                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1) {
                        //mBluetoothStatus.setText("Connected to Device: " + msg.obj);
                        Toast.makeText(getBaseContext(), "Connected to Device: " + msg.obj, Toast.LENGTH_SHORT).show();
                        btBluetooth.setBackgroundResource(R.drawable.ic_bluetooth_on);
                        btBluetooth.clearAnimation();
                    }

                    else
                        Toast.makeText(getBaseContext(), "Connect Fail: " + msg.obj, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void anhxa() {
        lvRoom = (ListView) findViewById(R.id.lvRoom);
        arrayRoom = new ArrayList<>();
        arrayRoom.add(new roomClass("BedRoom","3 Devices", R.drawable.bedroom));
        arrayRoom.add(new roomClass("Kitchen","3 Devices", R.drawable.kitchen));
        arrayRoom.add(new roomClass("Living Room","4 Devices", R.drawable.livingroom));
        arrayRoom.add(new roomClass("Bath Room","4 Devices", R.drawable.bathroom));
        arrayRoom.add(new roomClass("Gara","4 Devices", R.drawable.gara));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
        thread.interrupt();
    }

    private void initBroadcastReceiver() {
        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyBroadcastReceiver.ACTION_FIRST_ACTION);
        registerReceiver(myBroadcastReceiver, intentFilter);
    }

    private void updateData() {
        if (thread != null) {
            thread.interrupt();
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        int data = myBroadcastReceiver.data;
                        Log.e("TAG",String.valueOf(data));
                        if((data == 1) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("bật đèn phòng ngủ");
                        }
                        if((data == 0) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("tắt đèn phòng ngủ");
                        }
                        if((data == 3) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("mở cửa sổ phòng ngủ");
                        }
                        if((data == 2) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("đóng cửa sổ phòng ngủ");
                        }
                        if((data == 5) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("bật quạt phòng ngủ");
                        }
                        if((data == 4) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("tắt quạt phòng ngủ");
                        }
                        if((data == 7) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("bật đèn phòng bếp");
                        }
                        if((data == 6) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("tắt đèn phòng bếp");
                        }
                        if((data == 9) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("mở cửa sổ phòng bếp");
                        }
                        if((data == 8) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("đóng cửa sổ phòng bếp");
                        }
                        if((data == 11) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("bật quạt phòng bếp");
                        }
                        if((data == 10) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("tắt quạt phòng bếp");
                        }
                        if((data == 12) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("tắt đèn phòng khách");
                        }
                        if((data == 13) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("bật đèn phòng khách");
                        }
                        if((data == 14) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("đóng cửa sổ phòng khách");
                        }
                        if((data == 15) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("mở cửa sổ phòng khách");
                        }
                        if((data == 16) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("tắt quạt phòng khách");
                        }
                        if((data == 17) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("bật quạt phòng khách");
                        }
                        if((data == 18) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("đóng cửa chính");
                        }
                        if((data == 19) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("mở cửa chính");
                        }
                        if((data == 20) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("tắt đèn phòng tắm");
                        }
                        if((data == 21) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("bật đèn phòng tắm");
                        }
                        if((data == 22) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("tắt đèn gara ô tô");
                        }
                        if((data == 23) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("bật đèn gara ô tô");
                        }
                        if((data == 24) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("đóng cửa gara ô tô");
                        }
                        if((data == 25) && (data != old_data)){
                            if(mConnectedThread != null) mConnectedThread.write("mở cửa gara ô tô");
                        }
                        old_data = data;
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }
    public void openActivity2() {
        Intent intent = new Intent(this, Main2Activity.class);
        startActivity(intent);
    }
    public void openActivity3() {
        Intent intent = new Intent(this, Main3Activity.class);
        startActivity(intent);
    }
    public void openActivity4() {
        Intent intent = new Intent(this, Main4Activity.class);
        startActivity(intent);
    }
    public void openActivity5() {
        Intent intent = new Intent(this, Main5Activity.class);
        startActivity(intent);
    }
    public void openActivity6() {
        Intent intent = new Intent(this, Main6Activity.class);
        startActivity(intent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                    if(mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread.write(result.get(0));

                }
                break;
            }

        }
    }
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
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
    private void bluetoothOn(){
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }
    private void bluetoothOff(){
        mBTAdapter.disable(); // turn off
        Toast.makeText(getApplicationContext(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }
    private void discover(){
        // Check if the device is already discovering
        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }
    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };
    private void connectDevice(){
        if(!mBTAdapter.isEnabled()) {
            Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get the device MAC address, which is the last 17 chars in the View
        final String address = "00:18:E4:35:72:A7";
        final String name = "HC-05";

        // Spawn a new thread to avoid blocking the GUI one
        new Thread()
        {
            @Override
            public void run() {
                boolean fail = false;

                BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                try {
                    mBTSocket = createBluetoothSocket(device);
                } catch (IOException e) {
                    fail = true;
                    Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                }
                // Establish the Bluetooth socket connection.
                try {
                    mBTSocket.connect();
                } catch (IOException e) {
                    try {
                        fail = true;
                        mBTSocket.close();
                        mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                .sendToTarget();
                    } catch (IOException e2) {
                        //insert code to deal with this
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                }
                if(!fail) {
                    mConnectedThread = new ConnectedThread(mBTSocket, mHandler);
                    mConnectedThread.start();

                    mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                            .sendToTarget();
                }
            }
        }.start();
    }
    private void checkPermissions(BluetoothAdapter bluetoothAdapter) {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

    }
    private void sendNotification(String msg) {

        String channelId = "id group noti";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("message.notification?.title")
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(0 , builder.build());
        }

    }
}
