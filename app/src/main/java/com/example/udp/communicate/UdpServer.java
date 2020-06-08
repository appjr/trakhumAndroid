package com.example.udp.communicate;

import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Message;
import android.text.format.Formatter;

import androidx.annotation.RequiresApi;

import com.example.udp.MainActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class UdpServer extends Thread {

    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];
    MainActivity activity;
    private static int portNumber = 9000;

    public UdpServer(MainActivity activity) throws SocketException {
        this.activity = activity;
        socket = new DatagramSocket(portNumber);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void run() {
        running = true;
        sendTextToUI(MainActivity.SEND_TO_LOG,"Starting UDP Server "+getLocalIpAddress()+" on port "+portNumber);

        while (running) {
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
                sendTextToUI(MainActivity.SEND_TO_LOG,"Error: "+e.getLocalizedMessage());
            }
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buf, buf.length, address, port);
            String received
                    = new String(packet.getData(), 0, packet.getLength());
            sendTextToUI(MainActivity.SEND_TO_DATAFIELDS, parseText(received));
            try {
                sendMessageToServer(received);
            } catch (UnknownHostException e) {
                sendTextToUI(MainActivity.SEND_TO_LOG,"Error: "+e.getLocalizedMessage());
            }
        }
        socket.close();
    }

    public void sendMessageToServer(String msg) throws UnknownHostException {
        byte[] buf = msg.getBytes();
        String remoteServer = "192.168.0.161";
        int remotePort = 9000;
        InetAddress address = InetAddress.getByName(remoteServer);

        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, address, remotePort);
        try {
            socket.send(packet);
            sendTextToUI(MainActivity.SEND_TO_LOG,"Message Sent: "+msg+" to server "+remoteServer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendTextToUI(int id, Object text){
        if(text!=null) {
            Message msg = new Message();
            msg.what = id;
            msg.obj = text;
            if(msg.obj!=null) {
                MainActivity.handler.sendMessage(msg);
            }
        }
    }

    private String[] parseText(String text) {
        String fields[] = text.split(",");
        if(fields!=null && fields.length>9) {
            try {
                String toSplit = fields[0];
                String id = toSplit.split("\\.")[0];
                return new String[]{id, fields[7], fields[8], fields[9]};
            } catch (Exception e){
                return null;
            }
        } else return null;
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }


}