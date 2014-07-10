package com.bikestation.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.kdravolin.smartlock.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class AccountActivity extends ActionBarActivity {

    @InjectView(R.id.btEnter)
    Button btnEnter;
    @InjectView(R.id.etLogin)
    EditText etLogin;
    @InjectView(R.id.etPassword)
    EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ButterKnife.inject(this);

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences("bike", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("login", etLogin.getText().toString());
                editor.putString("password", etPassword.getText().toString());
                editor.commit();

                Intent intent = new Intent(AccountActivity.this, DeviceActivity.class);
                AccountActivity.this.startActivity(intent);
            }
        });
    }

}
