package com.example.felix.mylights;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class WriteThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final OutputStream mmOutStream;

    public WriteThread(BluetoothSocket socket) {
        mmSocket = socket;
        OutputStream tmpOut = null;

        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmOutStream = tmpOut;
    }


    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }


    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}
