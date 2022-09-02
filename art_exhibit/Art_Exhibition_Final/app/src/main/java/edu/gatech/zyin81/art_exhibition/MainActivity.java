package edu.gatech.zyin81.art_exhibition;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.Key;
import java.util.Arrays;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    Button btnAdmin;
//    Button btnPause;
//    Button btnReset;
    MediaPlayer[] music = null;

    private String m_Text = "";
    int thresh = 10;
    int lowerBound = -10;
    int UpperBound = thresh;
    int revHigherBound = 50;
    //    double set_radius0 = 600;//FIXME
//    double set_radius1 = 400;//FIXME
//    double set_radius2 = 400;//FIXME
//    double set_radius3 = 400;//FIXME
//    double set_radius4 = 400;//FIXME
//    double set_radius5 = 600;//FIXME
    double[] set_radius = {600, 400, 400, 400, 400, 600};//FIXME

    LooperThread looperThread;
    //    Key_point[] kp ;
//    Key_point kp1;
//    Key_point kp2;
//    Key_point kp3;
//    Key_point kp4;
//    Key_point kp5;
//    Key_point kp6;
    TextView txtInfo;
    FileInputStream inputStream;
    int previous_area = -1;
    int[] count;
    int[] reverse_count;
    ImageView imgPause;
    Key_point kp1 = new Key_point("music1.mp3");
    Key_point kp2 = new Key_point("music2.mp3");
    Key_point kp3 = new Key_point("music3.mp3");
    Key_point kp4 = new Key_point("music4.mp3");
    Key_point kp5 = new Key_point("music5.mp3");
    Key_point kp6 = new Key_point("music6.mp3");
    Key_point[] kp = new Key_point[]{kp1, kp2, kp3, kp4, kp5, kp6};
    int []pause_flag ;
    String [] art_names;
//    int puased_area = -1;

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
    };
    private UsbService usbService;
    private MyHandler mHandler;


    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    //    int state;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MediaPlayer mp1 = MediaPlayer.create(MainActivity.this, R.raw.ca);
        MediaPlayer mp2 = MediaPlayer.create(MainActivity.this, R.raw.bg);
        MediaPlayer mp3 = MediaPlayer.create(MainActivity.this, R.raw.ec);
        MediaPlayer mp4 = MediaPlayer.create(MainActivity.this, R.raw.eb);
        MediaPlayer mp5 = MediaPlayer.create(MainActivity.this, R.raw.ad);
        MediaPlayer mp6 = MediaPlayer.create(MainActivity.this, R.raw.nh);
//        imgPause.setImageResource(R.drawable.pause2);

        art_names = new String[] {"Origin\nby Chanell Angeli","Science of Happiness\nby Bojana Ginn","Diagrams of a Body in Space II\nby Emma Chammah","Sharing My Bed\nby Eve Brown","TechMyMoves\nby Ashutosh Dhekne","Heart Sounds Bench\nby Noura Howell"};


        music = new MediaPlayer[]{mp1,mp2,mp3,mp4,mp5,mp6};

        btnAdmin = findViewById(R.id.btnAdmin);
//        btnReset = findViewById(R.id.btnReset);
//        btnPause=findViewById(R.id.btn_pause);
        imgPause = findViewById(R.id.img_pause);
        pause_flag = new int[]{0,0,0,0,0,0};
        txtInfo = (TextView) findViewById(R.id.txt_intro);
//        txtInfo.setText("Welcome! Move near an exhibit to hear about it.");
        count = new int[]{0, 0, 0, 0, 0, 0, 0};
//        reverse_count = new int[]{0, 0, 0, 0, 0, 0, 0};


        //read file and assign tdoa variables
//            try (FileInputStream fis = new FileInputStream(new File(tdoaPath))) {//should change this read not write
////            for(int k=0;k<6;k++) {
////                for (int i = 0; i < 200; i++) {
////                    String store_line = "";
////                    for (int j = 0; j < kp[k].num_responders - 1; j++) {
////                        store_line = store_line + kp[k].recorded_loc[j][i] + ",";
////                    }
////                    store_line = store_line + kp[k].recorded_loc[kp[k].num_responders - 1][i];
//                int next = -1;
//                while (inputStream.available() != 0) {
//                    try {
//                        int[] byteArr = new int[100];
//                        int j = 0;
//                        next = inputStream.read();
//                        while (next != '\n') {
//                            byteArr[j] = next;
//                        }
//                        String cur_line = Arrays.toString(byteArr);
//                        txtInfo.setText(cur_line);
//
//                    } catch (IOException e) {
//                        Log.e("Exception", "File write failed:" + e.toString());
//                    }
//                }
//            } catch (IOException ioException) {
//                ioException.printStackTrace();
//            }
//        }

        // remaining bytes that can be read
//            System.out.println("Remaining bytes that can be read : " + fis.available());

//            int content;
//            // reads a byte at a time, if end of the file, tdoa_vecurns -1
//            while ((content = fis.read()) != -1) {
//                System.out.println((char) content);
//
//                System.out.println("Remaining bytes that can be read : " + fis.available());
//            }

//        btnReset.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                for(int i=0;i<6;i++)
//                {
//                    if(music[i].isPlaying()) {
//                        music[i].stop();
//                    }
//                }
//
//                music[0] = MediaPlayer.create(MainActivity.this, R.raw.sample1);
//                music[1] = MediaPlayer.create(MainActivity.this, R.raw.sample2);
//                music[2] = MediaPlayer.create(MainActivity.this, R.raw.sample3);
//                music[3] = MediaPlayer.create(MainActivity.this, R.raw.sample4);
//                music[4] = MediaPlayer.create(MainActivity.this, R.raw.sample5);
//                music[5] = MediaPlayer.create(MainActivity.this, R.raw.sample6);
//
//                previous_area = -1;
//                for(int i=0;i<7;i++)
//                {
//                    count[i]=0;
//                }
//
//            }
//        });

        btnAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Type Admin Password:");

// Set up the input
                final EditText input = new EditText(MainActivity.this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);

// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        if (m_Text.equals("z")) {
                            Intent intent_admin = new Intent(MainActivity.this, admin.class);
                            startActivity(intent_admin);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        imgPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int j=0;j<6;j++)
                {
                    if(count[j]>=thresh)
                    {
                        if(pause_flag[j]==0 && music[j].isPlaying())
                        {
                            music[j].pause();
                            pause_flag[j] = 1;
                            txtInfo.setText("Audio Paused");
                            imgPause.setImageResource(R.drawable.play2);
                        }

                        else if(pause_flag[j]==1 && !music[j].isPlaying())
                        {
                            music[j].start();
                            reset_flags(pause_flag);
                            txtInfo.setText(art_names[j]);

                        }
                    }
                }
            }
        });




        looperThread = new LooperThread();
        looperThread.start();

        mHandler = new MyHandler(Looper.getMainLooper(), MainActivity.this);
    }

    public void reset_flags(int[] pause_flag)
    {
        for(int i=0;i<pause_flag.length;i++)
        {
            pause_flag[i]=0;
        }
        imgPause.setImageResource(R.drawable.pause2);
    }


    public boolean check_all_isPlaying(MediaPlayer [] music)
    {
        for(int i=0;i<music.length;i++){
            if(music[i].isPlaying()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        File folder = new File(getExternalFilesDir("/").getAbsolutePath(), "ArtExhibition");
        if (!folder.exists()) {
            folder.mkdir();
        }
        for (int i = 0; i < 6; i++) {
            String tdoaPath = folder.getPath() + "/tdoa" + Integer.toString(i) + ".csv";
            Log.e("filepath: ", tdoaPath);
//            if (kp[i].load(tdoaPath) == false) {
//                Context context = getApplicationContext();
//                CharSequence text = "Data Missing";
//                int duration = Toast.LENGTH_SHORT;
//
//                Toast.makeText(context, text,
//                        duration).show();
//            }
        }
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
//        txtInfo.setText("service connected in start service");
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
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(Looper mainLooper, MainActivity activity) {
            mActivity = new WeakReference<>(activity);
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

            switch (msg.what) {
//                case UsbService.MESSAGE_FROM_SERIAL_PORT:
//                    String data = (String) msg.obj;
//                    mActivity.get().display.append("data" + data);
//                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.SYNC_READ:
//                    txtInfo.setText("Sync_Read");
                    String buffer = (String) msg.obj;//HERE IS THE ONE LINE MESSAGE FOR REAL TIME INPUT
//                    String newBuffer = buffer.replace(" ", "@");
                    line = line.concat(buffer);//concatenate each character to a string
//                    txtInfo.setText(line);

//                    boolean ignore=false;
                    if (line.contains(newline)) {//change this; line should come from file input
                        if (lineNum != 0) {
                            lineNum++;
                            //If localization is on
//                            if(state == 3)
//                            {

//                            txtInfo.setText(line);

                            int[] tdoa_vec = Key_point.parse_localization(line);

                            /*int[] tdoa_vec=new int[100];
                            String[] arrOfStr = line.split(",");
                            txtInfo.setText("");

//                            if (Key_point.allNumeric(arrOfStr)){
//                                for (int i=0;i<arrOfStr.length;i++) {
//                                    txtInfo.setText(txtInfo.getText() + ";" + arrOfStr[i]);
//                                }
//                            }

                            for (int i=0;i<arrOfStr.length;i++){
//                                txtInfo.setText(txtInfo.getText()+";"+arrOfStr[i]);
                                try{
                                    int temp=Key_point.parse_int_specialchar(arrOfStr[i]);

                                }catch (Exception e){
                                    ignore=true;
                                }
                            }
                            if(ignore==false){
                                for (int i=0;i<arrOfStr.length;i++) {
                                    txtInfo.setText(txtInfo.getText() + ";" + arrOfStr[i]);
                                }
                            }
//                            int[] tdoa_vec;
                            int size = arrOfStr.length;
//        if (size % 2 == 0) {//changed
                            if (size % 2 == 0 && !Key_point.hasChar(arrOfStr[0]) && !Key_point.hasChar(arrOfStr[size-1])) {//changed
//        if (size>4){
////        if((size-2)/2==this.num_responders) {
//            for (int i = 0; i < (size - 2) / 2; i++) {
//                int anchor_id = parse_int_specialchar(arrOfStr[2 * i + 1]);
//                if (anchor_id < this.num_responders + 1) {
//                    this.recorded_loc[anchor_id - 1][this.record_idx] = parse_int_specialchar(arrOfStr[2 * i + 2]);

                                int max_num_responders = Key_point.parse_int_specialchar(arrOfStr[0]);
                                tdoa_vec = new int[max_num_responders];
//                                tdoa_vec[0]=1;

                                for (int i = 0; i < (size - 2) / 2; i++) {
//                                    tdoa_vec[i]=2;
//                for (int i = 0; i < size / 2; i++) {
                                    int anchor_id = Key_point.parse_int_specialchar(arrOfStr[2 * i + 1]);
                                    if (anchor_id < max_num_responders + 1) {
                                        tdoa_vec[anchor_id - 1] = Key_point.parse_int_specialchar(arrOfStr[2 * i + 2]);
                                        txtInfo.setText("tdoa_vec: " + txtInfo.getText() + Integer.toString(tdoa_vec[anchor_id - 1]));
                                    }
                                }
                            }

//                                int[] tdoa_vet=tdoa_vec;
//                                tdoa_vecurn tdoa_vec;
//                            }else{
//                                tdoa_vecurn null;
//                            }
*/

//
//                            if (tdoa_vec!=null) {
//                                txtInfo.setText("first: " + tdoa_vec[0]);
//                            }
                            // decision tree

                            int i = -1;
                            if (tdoa_vec != null) {
//                                txtInfo.setText("tdoavev not null");
                                if(tdoa_vec[0]<-7261.5)
                                {
                                    i=0;
                                }else{
                                    if(tdoa_vec[2]<-7186.5)
                                    {
                                        i=5;
                                    }else{
                                        if(tdoa_vec[2]<-2366)
                                        {
                                            if(tdoa_vec[2]<-2867.5)
                                            {
                                                i=6;
                                            }else{
                                                if(tdoa_vec[0]<558)
                                                {
                                                    i=2;
                                                }else{
                                                    i=6;
                                                }
                                            }
                                        }else{
                                            if(tdoa_vec[0]<2768.5)
                                            {
                                                if(tdoa_vec[0]<-4582.5)
                                                {
                                                    i=1;
                                                }else{
                                                    if(tdoa_vec[2]<2849)
                                                    {
                                                        if(tdoa_vec[0]<-530.5)
                                                        {
                                                            if(tdoa_vec[0]<-3778.5)
                                                            {
                                                                i=6;
                                                            }else{
                                                                i=2;
                                                            }
                                                        }else{
                                                            i=3;
                                                        }
                                                    }else{
                                                        i=4;
                                                    }
                                                }
                                            }else{
                                                i=6;
                                            }
                                        }
                                    }
                                }

                                /*
                                if (tdoa_vec[0] < -7589.5) {
                                    if (tdoa_vec[2] < -1960) {
                                        i = 6;
                                    } else {
                                        i = 0;
                                    }
                                } else {
                                    if (tdoa_vec[2] < -7621) {
                                        if (tdoa_vec[0] < -1563) {
                                            i = 6;
                                        } else {
                                            i = 5;
                                        }
                                    } else {
                                        if (tdoa_vec[2] < -2554.5) {
                                            i = 6;
                                        } else {
                                            if (tdoa_vec[1] < 5448.5) {
                                                if (tdoa_vec[0] < -4131) {
                                                    i = 1;
                                                } else {
                                                    if (tdoa_vec[2] < 4691.5) {
                                                        if (tdoa_vec[1] > 1235.5) {
                                                            i = 2;
                                                        } else {
                                                            if (tdoa_vec[2] < 1331) {
                                                                i = 3;
                                                            } else {
                                                                i = 6;
                                                            }
                                                        }
                                                    } else {
                                                        i = 4;
                                                    }
                                                }
                                            } else {
                                                i = 6;
                                            }
                                        }
                                    }
                                }
                                */


//                                if (dist < set_radius[i]) {
                                for (int j = 0; j < 7; j++) {
                                    if (j == i) {
                                        count[j]++;
//                                        txtInfo.setText("kp: " + j + " count: " + count[j]);
//                                        reverse_count[j] = 0;
                                    } else {
                                        count[j]--;
//                                        reverse_count[j]++;
//                                        if (reverse_count[j] > revHigherBound) {
//                                            reverse_count[j] = revHigherBound;
//                                        }
                                    }

                                    if (count[j] > thresh && j!=previous_area) {
//                                        txtInfo.setText("")
                                        //                                                txtInfo.setText("Close to area "+Integer.toString(i));
//                                        txtInfo.setText("close to point " + Integer.toString(i) + " previous area "+Integer.toString(previous_area));

//                                        if (!check_all_isPlaying(music) ) {
//                                            if(j<6) {
//                                                music[j].start();
//                                            }
//
//                                        }

                                        if(previous_area>=0 && previous_area<6) {
                                            if (music[previous_area].isPlaying()) {
                                                music[previous_area].pause();
                                                txtInfo.setText("Welcome to Extension of Self!\nMove near an exhibit to hear about it.");

                                            }
                                        }

                                        if(j<6) {//6 is null space
//                                            txtInfo.setText(j+" music should start");
                                            music[j].start();
                                            reset_flags(pause_flag);
//                                            txtInfo.setText("Playing Audio for Display " + Integer.toString(j));
                                            txtInfo.setText(art_names[j]);
//                                            txtInfo.setText("j: "+j+" count: "+count[j]);
                                        }
                                        previous_area = j;


                                    }
//                                    if(j!=previous_area && count[j] > thresh){
////                                    if (previous_area != -1 && reverse_count[previous_area] > thresh) {
//                                        if (music[previous_area].isPlaying()) {
//                                            music[previous_area].pause();
//                                        }
//                                    }
//                                    if(j!=6) {
//                                        previous_area = j;
//                                    }

                                    if (count[j] < lowerBound) {
                                        count[j] = lowerBound;
                                    }
                                    if(count[j]>UpperBound){
                                        count[j] = UpperBound;

                                    }

                                }
                            }

                            //decision based on radius
                           /* for (int i = 0; i < 6; i++) {
                                if (kp[i].full) {//kp[i].full=false when first open the app
                                    double dist = kp[i].compute_distance(tdoa_vec);

//                                        if (i==0) {
//                                            txtInfo.setText("to point " + Integer.toString(i) + ": " + Double.toString(dist));
//                                        }
                                    if (dist < set_radius[i]) {
                                        count[i]++;
                                        reverse_count[i] = 0;
                                    } else {
                                        count[i]--;
                                        if (count[i] < lowerBound) {
                                            count[i] = lowerBound;
                                        }
                                        reverse_count[i]++;
                                        if (reverse_count[i] > revHigherBound) {
                                            reverse_count[i] = revHigherBound;
                                        }
                                    }

                                    if (count[i] > thresh) {
//                                                txtInfo.setText("Close to area "+Integer.toString(i));
                                        txtInfo.setText("to point " + Integer.toString(i) + ": " + Double.toString(dist));
                                        previous_area = i;
                                        if (!music.isPlaying()) {
                                            if (i == 0) {
                                                music = MediaPlayer.create(MainActivity.this, R.raw.sample1);
                                            } else if (i == 1) {
                                                music = MediaPlayer.create(MainActivity.this, R.raw.sample2);
                                            } else if (i == 2) {
                                                music = MediaPlayer.create(MainActivity.this, R.raw.sample3);
                                            } else if (i == 3) {
                                                music = MediaPlayer.create(MainActivity.this, R.raw.sample4);
                                            } else if (i == 4) {
                                                music = MediaPlayer.create(MainActivity.this, R.raw.sample5);
                                            } else if (i == 5) {
                                                music = MediaPlayer.create(MainActivity.this, R.raw.sample6);
                                            }
                                        }

                                        try {
//                                                    mp.setDataSource("/sdcard/Music/maine.mp3");//Write your location here
//                                                    music.prepare();
                                            music.start();

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                } else {
                                    txtInfo.setText("kp " + i + " is empty");
                                }
                            }
                            if (previous_area != -1 && reverse_count[previous_area] > thresh) {
                                if (music.isPlaying()) {
                                    music.stop();
                                }
                            }*/


//                        } else {
////                                txtInfo.setText("tdoa vec is null");
//                        }


                            // If recording is on
//                            for(int k=0;k<6;k++)
//                            {
//                                if(do_record[k])
//                                {
//                                    if(!kp[k].parse_new_line(line)) //finished recording
//                                    {
//                                        do_record[k] = false;
//                                        txtInfo.setText("Recording finished");
                            //read from each file
//                                        String filename =Integer.toString(k)+".csv";
//                                        File myFile = new File(folder,filename);
//                                        if(!myFile.exists()){
//                                            try {
//                                                myFile.createNewFile();
//
//                                            } catch (IOException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                        try {
//                                            fstream = new FileInputStream(myFile);
//                                        } catch (FileNotFoundException e) {
//                                            e.printStackTrace();
//                                        }

//                                        for(int i=0;i<200;i++)
//                                        {
//                                            String store_line = "";
//                                            for(int j = 0;j<kp[k].num_responders-1;j++) {
//                                                store_line = store_line + kp[k].recorded_loc[j][i]+",";
//                                            }
//                                            store_line = store_line + kp[k].recorded_loc[kp[k].num_responders-1][i];
//                                            try {
//                                                fstream.write(store_line.getBytes());
//                                                fstream.write('\n');
//                                            } catch (IOException e) {
//                                                Log.e("Exception", "File write failed:" + e.toString());
//                                            }
//                                        }

//                                        try {
//                                            fstream.close();
//                                        }
//                                        catch(IOException e){
//                                            Log.e("Exception", "File close failed:" + e.toString());
//                                        }
//                                    }
                            line = "";

                        } else {
                            lineNum++;
                            line = "";
                        }
                    }

                    break;
            }

            //}

        }
    }
}



