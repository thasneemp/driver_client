package com.launcher.mummu.cabclient.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.launcher.mummu.cabclient.R;

/**
 * Created by muhammed on 2/21/2017.
 */

public class TimeMorningFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.time_morning_fragment, container, false);
        return view;
    }
}
