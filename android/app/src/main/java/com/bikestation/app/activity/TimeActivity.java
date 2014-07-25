package com.bikestation.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import com.bikestation.app.BluetoothConnector;
import com.bikestation.app.ExtApp;
import com.bikestation.app.Lock;
import com.kdravolin.smartlock.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TimeActivity extends ActionBarActivity {

    @InjectView(R.id.btn_open)
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);
        ButterKnife.inject(this);

        BluetoothConnector connector = ExtApp.bluetoothConnector;
        if (connector.isConnected()) {
            setButtonConnectedState(true);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Lock.ReturnBikeTask task = new Lock.ReturnBikeTask(TimeActivity.this);
                task.execute();
            }
        });
    }

    public void setButtonConnectedState(boolean connected){
        if (connected){
            button.setText("Вернуть на стоянку");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Lock.ReturnBikeTask task = new Lock.ReturnBikeTask(TimeActivity.this);
                    task.execute();
                }
            });
        }else{
            button.setText("Найти велостоянку");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TimeActivity.this.finish();
                }
            });
        }
    }
}
