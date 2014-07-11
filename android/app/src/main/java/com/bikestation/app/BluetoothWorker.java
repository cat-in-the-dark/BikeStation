package com.bikestation.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.bikestation.app.activity.DeviceActivity;
import com.bikestation.app.adapter.DeviceAdapter;

public class BluetoothWorker {
    public static final int REQUEST_ENABLE_BT = 200;
    private static final String TAG = "Bluetooth";

    private BluetoothAdapter adapter;
    private DeviceAdapter lvAdapter;
    private DeviceActivity activity;

    public BluetoothWorker(DeviceActivity activity, DeviceAdapter lvAdapter) {
        this.activity = activity;
        this.lvAdapter = lvAdapter;
    }

    public void init() {
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            // TODO Show dialog "Device does not support Bluetooth" and close app
        }
        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            scan();
        }
    }

    public void scan() {
        Log.d(TAG, "Start scan");
        activity.setScanViewState(true);
//        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
//        if (pairedDevices.size() > 0) {
//            for (BluetoothDevice device : pairedDevices) {
//                lvAdapter.addDevice(device);
//                Log.d(TAG, "Add paired device: " + device.getName() + " | " + device.getAddress());
//            }
//        }

        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter endFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        activity.registerReceiver(receiver, foundFilter);
        activity.registerReceiver(receiver, endFilter);
        adapter.startDiscovery();
    }

    public void stop(){
        Log.d(TAG, "Stop scan");
        if (adapter != null) {
            adapter.cancelDiscovery();
        }
        activity.unregisterReceiver(receiver);
        activity.setScanViewState(false);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                lvAdapter.addDevice(device);
                Log.d(TAG, "Add device: " + device.getName() + " | " + device.getAddress());
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                activity.setScanViewState(false);
            }
        }
    };
}
