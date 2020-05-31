package com.example.udp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.udp.communicate.UdpServer;
import com.example.udp.ui.main.MainFragment;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    public static Handler handler;
    public final static int SEND_TO_LOG = 1;
    public final static int SEND_TO_DATAFIELDS = 2;

    UdpServer udpServer = null;
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }

        setupUIUpdates();
        try {
            udpServer = new UdpServer(this);
            udpServer.start();
        } catch (SocketException e) {
            e.printStackTrace();
            addText(e.getLocalizedMessage());
        }
    }

    private void setupUIUpdates() {

        try {
            addText(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        handler = new Handler(){
            public void handleMessage(android.os.Message msg) {
                TextView tv = (TextView) findViewById(R.id.logView);
                if(msg.what==SEND_TO_DATAFIELDS && msg.obj!=null) {
                    String [] fields = (String[]) msg.obj;
                    tv.append(fields[0]+" "+fields[1]+" "+fields[2]+" "+fields[3]+"\n");
                    updateUI(fields[0],fields[1],fields[2],fields[3]);
                } else {
                    if(msg!=null && msg.obj!=null) {
                        try {
                            tv.append((String) msg.obj);
                        } catch (Exception e){
                            String st;
                        }
                    }
                }
            }

            private void updateUI(String ID, String x, String y, String z){
                TextView tv = (TextView) findViewById(R.id.DeviceName1);
                String id1 = tv.getText().toString();
                if(id1.equalsIgnoreCase("") || id1.equalsIgnoreCase(ID)){
                    ((TextView) findViewById(R.id.DeviceName1)).setText(ID);
                    ((TextView) findViewById(R.id.x1v)).setText(x);
                    ((TextView) findViewById(R.id.y1v)).setText(y);
                    ((TextView) findViewById(R.id.z1v)).setText(z);
                } else{
                    ((TextView) findViewById(R.id.DeviceName2)).setText(ID);
                    ((TextView) findViewById(R.id.x2v)).setText(x);
                    ((TextView) findViewById(R.id.y2v)).setText(y);
                    ((TextView) findViewById(R.id.z2v)).setText(z);
                }
            }
        };
    }


    private void addText(String txt){
        TextView tv = (TextView) findViewById(R.id.logView);
        tv.append(txt+"\n");
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}
