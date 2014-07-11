package com.bikestation.app.activity;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bikestation.app.Lock;
import com.bikestation.app.adapter.BikesAdapter;
import com.kdravolin.smartlock.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class BikesActivity extends ActionBarActivity {

    @InjectView(R.id.lv_bikes) ListView lvBikes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bikes);
        ButterKnife.inject(this);
        BikesAdapter adapter = new BikesAdapter(this);
        lvBikes.setAdapter(adapter);
        lvBikes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        SharedPreferences settings = getSharedPreferences("bike", 0);
        String login = settings.getString("login", null);
        String password = settings.getString("password", null);

        Lock.BikesTask task = new Lock.BikesTask(login, password, adapter);
        task.execute();
    }

}
