package com.example.smarthome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class roomAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private List<roomClass> roomList;

    public roomAdapter(Context context, int layout, List<roomClass> roomList) {
        this.context = context;
        this.layout = layout;
        this.roomList = roomList;
    }


    @Override
    public int getCount() {
        return roomList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(layout, null);
        TextView txtName = (TextView) convertView.findViewById(R.id.txtName);
        TextView txtDevice = (TextView) convertView.findViewById(R.id.txtDevices);
        ImageView imgView = convertView.findViewById(R.id.imgView);
        roomClass roomClass = roomList.get(position);
        txtName.setText(roomClass.getName());
        txtDevice.setText(roomClass.getDevice());
        imgView.setImageResource(roomClass.getImage());
        return convertView;
    }
}
