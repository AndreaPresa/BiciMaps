package com.example.michel.bicimaps;
/**
 * Created by Michel on 21/04/2018.
 */

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class LocationHolder extends RecyclerView.ViewHolder {

    private View mView;

    public LocationHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void setLatitud(double lat) {
        TextView field = (TextView) mView.findViewById(R.id.lblLat);
        String latitud = Double.toString(lat);
        field.setText(latitud);
    }

    public void setLongitud(double lon) {
        TextView field1 = (TextView) mView.findViewById(R.id.lblLong);
        String longitud = Double.toString(lon);
        field1.setText(longitud);    }


    public void setPM(int pm) {
        TextView field1 = (TextView) mView.findViewById(R.id.lblPM);
        String longitud = Integer.toString(pm);
        field1.setText(longitud);    }

    public void setDH(String dh) {
        TextView field1 = (TextView) mView.findViewById(R.id.lblFecha);
        field1.setText(dh);    }

}
