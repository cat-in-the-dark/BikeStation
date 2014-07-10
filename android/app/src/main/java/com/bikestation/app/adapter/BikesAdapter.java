package com.bikestation.app.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kdravolin.smartlock.app.R;
import com.bikestation.app.User;

import java.util.ArrayList;
import java.util.List;

public class BikesAdapter extends BaseAdapter {

    private List<String> bikes;
    private final Activity context;

    public BikesAdapter(Activity context) {
        this.context = context;
        bikes = new ArrayList<String>();
    }

    @Override
    public int getCount() {
        return bikes.size();
    }

    @Override
    public Object getItem(int i) {
        return bikes.get(i);
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
            rowView = inflater.inflate(R.layout.item_bike, null, true);
        }
        TextView tvAccountName = (TextView)rowView.findViewById(R.id.tv_account_name);
        tvAccountName.setText(bikes.get(i));
        return rowView;
    }

    public void addBike(String bike){
        bikes.add(bike);
    }

}
