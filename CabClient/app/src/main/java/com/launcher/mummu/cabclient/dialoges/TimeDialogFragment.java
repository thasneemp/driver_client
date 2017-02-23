package com.launcher.mummu.cabclient.dialoges;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;

import com.launcher.mummu.cabclient.R;
import com.launcher.mummu.cabclient.adapters.TimeAdapter;
import com.launcher.mummu.cabclient.fragments.TimeEveningFragment;
import com.launcher.mummu.cabclient.fragments.TimeMorningFragment;

import java.util.ArrayList;

/**
 * Created by muhammed on 2/1/2017.
 */

public class TimeDialogFragment extends AppCompatDialogFragment implements ViewPager.OnPageChangeListener, View.OnClickListener {
    private TextView buttonTextView;
    private ViewPager mViewPager;
    private CheckBox mMorningCheckBox;
    private CheckBox mEveningCheckBox;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(STYLE_NO_FRAME, R.style.transparent_dialog_borderless);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.time_dialog_fragment, container, false);

        buttonTextView = (TextView) view.findViewById(R.id.buttonTextView);
        buttonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mMorningCheckBox = (CheckBox) view.findViewById(R.id.morningCheckButton);
        mEveningCheckBox = (CheckBox) view.findViewById(R.id.eveningCheckButton);


        mMorningCheckBox.setOnClickListener(this);
        mEveningCheckBox.setOnClickListener(this);
        TimeAdapter timeAdapter = new TimeAdapter(getChildFragmentManager(), getFragments());
        mViewPager.setAdapter(timeAdapter);
        mViewPager.addOnPageChangeListener(this);
        return view;
    }

    private ArrayList<Fragment> getFragments() {
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new TimeMorningFragment());
        fragments.add(new TimeEveningFragment());
        return fragments;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
//        getDialog().getWindow()
//                .getAttributes().windowAnimations = R.style.MyAnimation_Window;
    }

    @Override
    public void onResume() {
        Window window = getDialog().getWindow();
        Point size = new Point();

        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);

        int width = size.x;

        window.setLayout((int) (width * 0.85), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        super.onResume();
    }

    public void setOnClickListener(View.OnClickListener listener) {
        buttonTextView.setOnClickListener(listener);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
                changeValues(true, false);

                break;
            case 1:
                changeValues(false, true);
                break;
        }

    }

    public void changeValues(boolean morning, boolean evening) {
        mMorningCheckBox.setChecked(morning);
        mEveningCheckBox.setChecked(evening);

        if (morning) {
            mMorningCheckBox.setTextColor(getResources().getColor(R.color.white));
        } else {
            mMorningCheckBox.setTextColor(getResources().getColor(R.color.black));
        }
        if (evening) {
            mEveningCheckBox.setTextColor(getResources().getColor(R.color.white));
        } else {
            mEveningCheckBox.setTextColor(getResources().getColor(R.color.black));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.morningCheckButton:
                changeValues(true, false);
                mViewPager.setCurrentItem(0);
                break;
            case R.id.eveningCheckButton:
                changeValues(false, true);
                mViewPager.setCurrentItem(1);
                break;
        }
    }
}
