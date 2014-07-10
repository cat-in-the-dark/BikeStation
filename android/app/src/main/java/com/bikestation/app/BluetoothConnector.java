package com.bikestation.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnector {
    private static final UUID uuid = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket bluetoothSocket;
    private InputStream input;
    private OutputStream output;
    String name;

    public void connect(String address) throws IOException {
        if (bluetoothSocket != null)
            disconnect();

        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        final BluetoothDevice remoteDevice = bluetoothAdapter
                .getRemoteDevice(address);
        bluetoothSocket = remoteDevice
                .createInsecureRfcommSocketToServiceRecord(uuid);
        bluetoothSocket.connect();
        input = bluetoothSocket.getInputStream();
        output = bluetoothSocket.getOutputStream();
        Log.v("Bluetooth", "connection established");
        name = remoteDevice.getName();
    }

    public void disconnect() throws IOException {
        if (bluetoothSocket == null)
            return;
        else {
            bluetoothSocket.close();
            bluetoothSocket = null;
        }
        if (input != null) {
            input.close();
            input = null;
        }
        if (output != null) {
            output.close();
            output = null;
        }
        name = null;
    }

    public void send(String msg) throws IOException {
        output.write(msg.getBytes());
        output.flush();
    }

    public String recieve() throws IOException {
        byte[] buffer = new byte[1024]; // buffer store for the stream
        input.read(buffer);
        // Log.d("MbLock", new String(buffer, "UTF-8"));
        String msg = (new String(buffer, "UTF-8").trim());
        return msg;
    }
}

