package com.bikestation.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bikestation.app.activity.BikesActivity;
import com.bikestation.app.activity.TimeActivity;
import com.bikestation.app.adapter.BikesAdapter;
import com.kdravolin.smartlock.app.R;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class Lock {

    public static class ConnectTask extends AsyncTask<String, String, String> {

        String status;
        String onBike;
        ListView lvDevices;
        View itemView;
        ImageView img;
        ProgressBar prgBar;
        String login;
        String password;
        String address;
        Activity activity;

        public ConnectTask(Activity activity, ListView lvDevices, View itemView,
                           String login, String password, String address) {
            this.activity = activity;
            this.lvDevices = lvDevices;
            this.itemView = itemView;
            this.img = (ImageView) itemView.findViewById(R.id.img_device_status);
            this.prgBar = (ProgressBar) itemView.findViewById(R.id.prgbar_device_load);
            this.login = login;
            this.address = address;
            this.password = password;
        }

        protected void onPreExecute() {
            lvDevices.setEnabled(false);
            prgBar.setVisibility(View.VISIBLE);
            img.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            BluetoothConnector connector = ExtApp.bluetoothConnector;

            try {
                connector.connect(address);
                JSONObject helloRequest = new JSONObject();
                helloRequest.put("command", "hello");
                helloRequest.put("login", login);
                helloRequest.put("PIN", password);
                connector.send(helloRequest.toString());

                String response = connector.recieve();
                JSONParser parser = new JSONParser();
                JSONObject responseJson;
                try {
                    responseJson = (JSONObject)parser.parse(response);
                } catch (ParseException e) {
                    return null;
                }
                status = responseJson.get("status").toString();
                if (!status.equals("OK"))
                    return null;
                onBike = responseJson.get("onBike").toString();
            } catch (IOException e) {
                Log.e("Bluetooth", "ConnectionTask error" + e.getMessage());
                try {
                    connector.disconnect();
                } catch (IOException e1) {
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            lvDevices.setEnabled(true);
            prgBar.setVisibility(View.INVISIBLE);

            if (status != null) {
                if (!status.equals("OK")) {
                    img.setImageResource(R.drawable.forbidden);
                } else {
                    Class activityClass;
                    if (onBike.equals("true"))
                        activityClass = TimeActivity.class;
                    else
                        activityClass = BikesActivity.class;

                    img.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(activity, activityClass);
                    intent.putExtra("login", login);
                    intent.putExtra("password", password);
                    activity.startActivity(intent);
                }
            } else {
                img.setVisibility(View.VISIBLE);
                itemView.setEnabled(false);
            }
        }
    }

    public static class BikesTask extends AsyncTask<String, String, String> {

        private final BikesAdapter adapter;

        public BikesTask(BikesAdapter adapter){
            this.adapter = adapter;
        }

        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... strings) {
            BluetoothConnector connector = ExtApp.bluetoothConnector;
            try {
                JSONObject helloRequest = new JSONObject();
                helloRequest.put("command", "getBikeList");
                connector.send(helloRequest.toString());

                String response = connector.recieve();
                JSONParser parser = new JSONParser();
                JSONObject responseJson;
                try {
                    responseJson = (JSONObject)parser.parse(response);
                } catch (ParseException e) {
                    return null;
                }
                String status = responseJson.get("status").toString();
                if (!status.equals("OK"))
                    return null;

                JSONArray array = (JSONArray)responseJson.get("bikeList");
                for (Object bike : array){
                    adapter.addBike(bike.toString());
                }
            } catch (IOException e) {
                Log.e("Bluetooth", "ConnectionTask error" + e.getMessage());
                try {
                    connector.disconnect();
                } catch (IOException e1) {
                    return null;
                }
            }
            return null;
        }

        protected void onPostExecute(String result) {
            adapter.notifyDataSetChanged();
        }
    }

    public static class GetBikeTask extends AsyncTask<String, String, String> {

        private final Activity activity;
        private final String bikeId;
        private String status;

        public GetBikeTask(Activity activity, String bikeId){
            this.activity = activity;
            this.bikeId = bikeId;
        }

        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... strings) {
            BluetoothConnector connector = ExtApp.bluetoothConnector;
            try {
                JSONObject helloRequest = new JSONObject();
                helloRequest.put("command", "selectBike");
                helloRequest.put("bikeId", bikeId);
                connector.send(helloRequest.toString());

                String response = connector.recieve();
                JSONParser parser = new JSONParser();
                JSONObject responseJson;
                try {
                    responseJson = (JSONObject)parser.parse(response);
                } catch (ParseException e) {
                    return null;
                }
                status = responseJson.get("status").toString();
                if (!status.equals("OK"))
                    return null;
            } catch (IOException e) {
                Log.e("Bluetooth", "ConnectionTask error" + e.getMessage());
                try {
                    connector.disconnect();
                } catch (IOException e1) {
                    return null;
                }
            }
            return null;
        }

        protected void onPostExecute(String result) {
            if ("OK".equals(status)) {
//                SharedPreferences settings = getSharedPreferences("bike", 0);
//                SharedPreferences.Editor editor = settings.edit();
//                editor.putString("login", etLogin.getText().toString());
                Intent intent = new Intent(activity, TimeActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }
        }
    }

    public static class ReturnBikeTask extends AsyncTask<String, String, String> {

        private final Activity activity;
        private String status;

        public ReturnBikeTask(Activity activity){
            this.activity = activity;
        }

        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... strings) {
            BluetoothConnector connector = ExtApp.bluetoothConnector;
            try {
                JSONObject helloRequest = new JSONObject();
                helloRequest.put("command", "returnBike");
                connector.send(helloRequest.toString());

                String response = connector.recieve();
                JSONParser parser = new JSONParser();
                JSONObject responseJson;
                try {
                    responseJson = (JSONObject)parser.parse(response);
                } catch (ParseException e) {
                    return null;
                }
                status = responseJson.get("status").toString();
                if (!status.equals("OK"))
                    return null;
            } catch (IOException e) {
                Log.e("Bluetooth", "ConnectionTask error" + e.getMessage());
                try {
                    connector.disconnect();
                } catch (IOException e1) {
                    return null;
                }
            }
            return null;
        }

        protected void onPostExecute(String result) {
            try {
                ExtApp.bluetoothConnector.disconnect();
            } catch (IOException e) {
            }
            Context context = activity.getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            CharSequence text;
            if ("OK".equals(status)) {
                text = "Вы вернули велосипед. Спасибо за использование сервиса.";
                ((TimeActivity)activity).setButtonConnectedState(true);
            } else {
                text = "Ошибка соединения. Пожалуйста попробуйте подключиться снова.";
                ((TimeActivity)activity).setButtonConnectedState(false);
            }
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }
}

