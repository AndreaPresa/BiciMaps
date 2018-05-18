package com.example.michel.bicimaps; /**
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

   /* public void setPosition (int pos) {
        TextView field0 = (TextView) mView.findViewById(R.id.lblPos);
        field0.setText(pos);
    }
*/
    public void setLatitud(double lat) {
        TextView field = (TextView) mView.findViewById(R.id.lblLat);
        String latitud = Double.toString(lat);
        field.setText(latitud);
    }

    public void setLongitud(double lon) {
        TextView field1 = (TextView) mView.findViewById(R.id.lblLong);
        String longitud = Double.toString(lon);
        field1.setText(longitud);    }
}
