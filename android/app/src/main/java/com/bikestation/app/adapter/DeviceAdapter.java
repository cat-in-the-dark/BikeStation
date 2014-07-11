package com.bikestation.app.adapter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kdravolin.smartlock.app.R;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends BaseAdapter {

    private List<BluetoothDevice> devices;
    private final Activity context;

    public DeviceAdapter(Activity context) {
        this.context = context;
        this.devices = new ArrayList<BluetoothDevice>();
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int i) {
        return devices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = view;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.item_device, null, true);
        }

        TextView tvAccountName = (TextView)rowView.findViewById(R.id.tv_device_name);
        tvAccountName.setText(devices.get(i).getName());

//        if (i==2) {
//            ProgressBar prgbarLock = (ProgressBar) rowView.findViewById(R.id.prgbar_device_load);
//            prgbarLock.setVisibility(View.VISIBLE);
//        }
        return rowView;
    }

    public void addDevice(BluetoothDevice device){
        for (BluetoothDevice d : devices){
            if (d.getAddress().equals(device.getAddress())
                    && d.getName().equals(device.getName()))
                return;
        }
        devices.add(device);
        notifyDataSetChanged();
    }
}
