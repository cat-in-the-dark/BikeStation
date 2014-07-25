package com.bikestation.app.activity;

import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bikestation.app.Lock;
import com.bikestation.app.adapter.BikesAdapter;
import com.bikestation.app.adapter.DeviceAdapter;
import com.kdravolin.smartlock.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class BikesActivity extends ActionBarActivity {

    @InjectView(R.id.lv_bikes) ListView lvBikes;
    BikesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bikes);
        ButterKnife.inject(this);
        adapter = new BikesAdapter(this);
        lvBikes.setAdapter(adapter);
        lvBikes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BikesAdapter adapter = (BikesAdapter) parent.getAdapter();
                String bikeId = (String) adapter.getItem(position);

                Lock.GetBikeTask task = new Lock.GetBikeTask(BikesActivity.this, bikeId);
                task.execute();
            }
        });

        Lock.BikesTask task = new Lock.BikesTask(adapter);
        task.execute();
    }

}
