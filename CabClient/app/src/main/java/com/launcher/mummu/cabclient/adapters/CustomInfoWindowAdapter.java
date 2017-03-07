package com.launcher.mummu.cabclient.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.launcher.mummu.cabclient.R;

/**
 * Created by muhammed on 3/7/2017.
 */

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public CustomInfoWindowAdapter(Context context) {
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {

        return getView(marker);
    }

    private View getView(Marker marker) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.info_window_adapter, null, false);
        TextView mTitleTextView = (TextView) view.findViewById(R.id.textView5);
        mTitleTextView.setText(marker.getTitle());
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return getView(marker);
    }

    public Context getContext() {
        return context;
    }
}
