package com.example.michel.bicimaps;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class BTDevicesAdapter extends ArrayAdapter<String> {

    private Activity mContext;
    private ArrayList<String> mNames;
    private ArrayList<String> mAddresses;


    //The ArrayAdapter constructor
    public BTDevicesAdapter(Activity context, ArrayList<String> names, ArrayList<String> values) {
        super(context, R.layout.layout_btadapter, values);
        //Set the value of variables
        mNames = names;
        mAddresses = values;
    }

    //Here the ListView will be displayed
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutView = inflater.inflate(R.layout.layout_btadapter, null, true);
        TextView mTextView = (TextView) layoutView.findViewById(R.id.adapter_text);
        mTextView.setText(mNames.get(position));
        TextView mTextView1 = (TextView) layoutView.findViewById(R.id.adapter_address);
        mTextView1.setText(mAddresses.get(position));
        return layoutView;
    }
}