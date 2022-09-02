package edu.gatech.zyin81.art_exhibition;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Executor;

import edu.gatech.zyin81.art_exhibition.UsbService;

public class admin extends AppCompatActivity {
    Button btnDone;
    Button bt1;
    Button bt2;
    Button bt3;
    Button bt4;
    Button bt5;
    Button bt6;
    TextView txtInfo;
    int stopWriting=0;
    int record_size=1000; //change me

    LooperThread looperThread;

    Key_point[] kp;
    Key_point kp1;
    Key_point kp2;
    Key_point kp3;
    Key_point kp4;
    Key_point kp5;
    Key_point kp6;
    File folder;
    private edu.gatech.zyin81.art_exhibition.UsbService usbService; //uncomment

    FileOutputStream fstream;
    FileOutputStream lineStream;
    int state;//0-idle, 1-recording, 2-load, 3-loc
    int[] count;
    int[] reverse_count;
    int thresh = 10;
    int previous_area = -1;

    double set_radius = 600;//FIXME

    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };//uncomment
    //    private UsbService usbService; //uncomment
    private TextView display;
    private EditText editText;
    private CheckBox box9600, box38400;
    private MyHandler mHandler;
//    private Executor mHandler;
    private Object udpclient;
    public static Handler exHandler;
    private int radius = 1200;
    private Bitmap curr_img = null;
    boolean do_record[] = new boolean[]{false, false, false, false, false, false};

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);//uncomment
//            Log.w("onserviceconnect: ",usbService.baud)
            txtInfo.setText("service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null; //uncomment
            txtInfo.setText("disconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        btnDone = findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        kp1 = new Key_point("kp1.csv");
        kp2 = new Key_point("kp2.csv");
        kp3 = new Key_point("kp3.csv");
        kp4 = new Key_point("kp4.csv");
        kp5 = new Key_point("kp5.csv");
        kp6 = new Key_point("kp6.csv");
        kp = new Key_point[]{kp1, kp2, kp3, kp4, kp5, kp6};
        txtInfo = (TextView) findViewById(R.id.txtInfo);
        count = new int[]{0, 0, 0, 0, 0, 0};
        reverse_count = new int[]{0, 0, 0, 0, 0, 0};
        ActivityCompat.requestPermissions(admin.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 23);
        ActivityCompat.requestPermissions(admin.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 23);

        folder = new File(getExternalFilesDir("/").getAbsolutePath(), "ArtExhibition");
        if (!folder.exists()) {
            folder.mkdir();
        }

        exHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String msgString = (String) msg.obj;
                Log.d("Handler", "Now in Handler");

//                txt44.setText(null);
//                txt44.setText("Receive: " + msgString);
                //Testing
//                imtest.setVisibility(View.VISIBLE);
//                if(curr_img!=null)
//                    imtest.setImageBitmap(curr_img);

            }
        };

        bt1 = (Button) findViewById(R.id.buttonRec1);
        bt2 = (Button) findViewById(R.id.buttonRec2);
        bt3 = (Button) findViewById(R.id.buttonRec3);
        bt4 = (Button) findViewById(R.id.buttonRec4);
        bt5 = (Button) findViewById(R.id.buttonRec5);
        bt6 = (Button) findViewById(R.id.buttonRec6);

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                do_record[0] = true;
//                state = 1;
                txtInfo.setText("Recording 1");
            }
        });

        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                do_record[1] = true;
//                state = 1;
                txtInfo.setText("Recording 2");


            }
        });

        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                do_record[2] = true;
//                state = 1;
                txtInfo.setText("Recording 3");

            }
        });

        bt4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                do_record[3] = true;
//                state = 1;
                txtInfo.setText("Recording 2");


            }
        });

        bt5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                do_record[4] = true;
//                state = 1;

            }
        });

        bt6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                do_record[5] = true;
//                state = 1;
                txtInfo.setText("Recording 6");
            }
        });

        looperThread=new LooperThread();
        looperThread.start();

        mHandler=new MyHandler(Looper.getMainLooper(),admin.this);

//        mHandler = new MyHandler(admin.this);//changed
//        txtInfo.setText("handler created");
        String lineFile="lines.csv";//change
        File lFile = new File(folder, lineFile);
        if (!lFile.exists()) {
            try {
                lFile.createNewFile();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            lineStream = new FileOutputStream(lFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
//        txtInfo.setText("onresume");
//        usbService.changeBaudRate(9600);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver); //uncomment
        unbindService(usbConnection);

    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
//            txtInfo.setText("service connected in start service");
            Intent intentstartService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    intentstartService.putExtra(key, extra);
                }
            }
            startService(intentstartService);
        }
        txtInfo.setText("service connected in start service");
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
//        usbService.changeBaudRate(9600);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter); //uncomment
    }


    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    class MyHandler extends Handler {
//    class MyHandler extends Executor {
        //        private final WeakReference<MainActivity> mActivity;
        private final WeakReference<admin> mActivity;

        public MyHandler(Looper looper,admin activity) {
            mActivity = new WeakReference<>(activity);
            txtInfo.setText("here0");
        }

        int lineNum = 0;//skip the first line
        String line = "";
        String newline = System.getProperty("line.separator");


        int prevx = 0;
        int prevy = 0;

        int invisible_flag = 1;

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void handleMessage(Message msg) {
//            txtInfo.setText("here1");
            int t = 0;
            if (t == 0) {


//            }
//            return;
                switch (msg.what) { //uncomment


                    case UsbService.MESSAGE_FROM_SERIAL_PORT:
                        String data = (String) msg.obj;
//                    mActivity.get().display.append("data" + data);
                        txtInfo.setText("data" + data);
                        return;


                    case UsbService.CTS_CHANGE:
                        txtInfo.setText("here2");
                        Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                        break;
                    case UsbService.DSR_CHANGE:
                        txtInfo.setText("here3");
                        Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                        break;
                    case UsbService.SYNC_READ:
//                        txtInfo.setText("here4");
//                        char[] byteBuffer= ((String)msg.obj).toCharArray();
//                        Log.w("byteBuffer: ",byteBuffer.toString());//changed
//                        txtInfo.setText(buffer);
                        String buffer = (String) msg.obj;//HERE IS THE ONE LINE MESSAGE FOR REAL TIME INPUT
//                        txtInfo.setText("buffer: "+buffer);
//                        String newBuffer = buffer.replace(" ", "@");

//                        line = line.concat(newBuffer);//concatenate each character to a string
                        line = line.concat(buffer);
//                        line=line+buffer.getBytes(StandardCharsets.UTF_8);
//                        line=line+buffer.toCharArray();
                        txtInfo.setText("buffer: "+line);
//                        byte[] tempArray = line.getBytes();
//                        String correctLine=new String(tempArray, StandardCharsets.UTF_8);

//                        Log.w("Line: ",line);

//                        txtInfo.setText(line);
//                        Log.w("edu.gatech.zyin81",line.getBytes());

                        try {
//                            lineStream.write(line.getBytes());
//                            lineStream.write(line.getBytes(StandardCharsets.UTF_8));
                            if (stopWriting==0) {
                                lineStream.write(line.getBytes());
                            }
//                            Log.w("write Successful","yay");
//                            fstream.write('\n');
//                                                    txtInfo.setText(Integer.toString(kp[k].record_idx));

                        } catch (IOException e) {
                            Log.e("Exception", "File write failed:" + e.toString());
                            Log.w("enough data collected","done");
                        }

                        if (line.contains(newline)) {
                            if (lineNum != 0) {
                                lineNum++;
                                //If localization is on
//                            if(state == 3)
//                            {
//                                int[] tdoa_vec = Key_point.parse_localization(line);
//                                if(tdoa_vec!=null)
//                                {
//                                    for(int i=0;i<6;i++)
//                                    {
//                                        if(kp[i].full)
//                                        {
//                                            double dist = kp[i].compute_distance(tdoa_vec);
//                                            txtInfo.setText(Double.toString(dist));
//
//                                        }
//                                    }
//                                }
//                            }

                                // If recording is on
                                for (int k = 0; k < 6; k++) {
                                    if (do_record[k]) {

                                        if (!kp[k].parse_new_line(line)) //finished recording
                                        {
                                            do_record[k] = false;
                                            txtInfo.setText("Recording finished");

//                                            String filename = Integer.toString(k) + ".csv";
                                            String tdoaFile="tdoa"+Integer.toString(k)+".csv";//change
                                            File myFile = new File(folder, tdoaFile);
                                            if (!myFile.exists()) {
                                                try {
                                                    myFile.createNewFile();

                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            try {
                                                fstream = new FileOutputStream(myFile);
                                            } catch (FileNotFoundException e) {
                                                e.printStackTrace();
                                            }

                                            for (int i = 0; i < record_size; i++) {
                                                String store_line = "";
                                                for (int j = 0; j < kp[k].num_responders - 1; j++) {
                                                    store_line = store_line + kp[k].recorded_loc[j][i] + ",";
                                                }
                                                store_line = store_line + kp[k].recorded_loc[kp[k].num_responders - 1][i];
//                                                txtInfo.setText(store_line);
                                                Log.w("tobestored: ",store_line);
                                                try {
//                                                byte[] cur_bytes=store_line.getBytes();
//                                                    fstream.write((cur_bytes.length));
//                                                    fstream.write("#".getBytes());
                                                    fstream.write(store_line.getBytes());
                                                    fstream.write('\n');
//                                                    txtInfo.setText(Integer.toString(kp[k].record_idx));

                                                } catch (IOException e) {
                                                    Log.e("Exception", "File write failed:" + e.toString());
                                                }
                                            }


                                            try {
                                                fstream.close();
                                            } catch (IOException e) {
                                                Log.e("Exception", "File close failed:" + e.toString());
                                            }

                                            try {
                                                lineStream.close();
                                                stopWriting = 1;
                                                Log.e("Exception", "File closed");
                                            } catch (IOException e) {
                                                Log.e("Exception", "File close failed:" + e.toString());
                                            }
                                        } else {
                                            txtInfo.setText("recording " + Integer.toString(k) + ", " + Integer.toString(kp[k].record_idx));
                                        }
                                    }
                                }
                                line = "";

                            } else {
                                lineNum++;
                                line = "";
                            }
                        }
                        break;
                }

            }
        }
    }
}
