package com.bikestation.app.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bikestation.app.BluetoothWorker;
import com.bikestation.app.Lock;
import com.kdravolin.smartlock.app.R;
import com.bikestation.app.adapter.DeviceAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DeviceActivity extends ActionBarActivity {

    @InjectView(R.id.lv_devices)
    ListView lvDevices;
    @InjectView(R.id.scanning_view)
    View vwScanning;

    private String login;
    private BluetoothWorker bluetoothWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        ButterKnife.inject(this);

        Intent intent = getIntent();
        login = intent.getStringExtra("login");

        setScanViewState(false);
        DeviceAdapter adapter = new DeviceAdapter(this);
        lvDevices.setAdapter(adapter);
        lvDevices.setOnItemClickListener(listener);

        bluetoothWorker = new BluetoothWorker(this, adapter);
        bluetoothWorker.init();
    }

    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            DeviceAdapter adapter = (DeviceAdapter) adapterView.getAdapter();
            BluetoothDevice device = (BluetoothDevice) adapter.getItem(i);
            Log.d("Bluetooth", "Select " + device.getName());

            SharedPreferences settings = getSharedPreferences("bike", 0);
            String login = settings.getString("login", null);
            String password = settings.getString("password", null);

            Lock.ConnectTask connect = new Lock.ConnectTask(DeviceActivity.this,
                    lvDevices, view, login, password, device.getAddress());
            connect.execute();
        }
    };

    public void setScanViewState(boolean isScan) {
        if (isScan) {
            vwScanning.setVisibility(View.VISIBLE);
            lvDevices.setEnabled(false);
        } else {
            vwScanning.setVisibility(View.GONE);
            lvDevices.setEnabled(true);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothWorker.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                bluetoothWorker.scan();
            } else {
                this.finish();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (vwScanning.getVisibility() == View.VISIBLE) {
            bluetoothWorker.stop();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
