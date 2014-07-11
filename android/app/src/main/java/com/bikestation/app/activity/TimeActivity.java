package com.bikestation.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

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


        Intent intent = getIntent();
        String status = intent.getStringExtra("status");
        String login = intent.getStringExtra("login");
        String password = intent.getStringExtra("password");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = TimeActivity.this.getIntent();
                String login = intent.getStringExtra("login");
                String password = intent.getStringExtra("password");
//                Lock.OpenTask open = new Lock.OpenTask(login, password, button);
//                open.execute();
            }
        });
    }
}
