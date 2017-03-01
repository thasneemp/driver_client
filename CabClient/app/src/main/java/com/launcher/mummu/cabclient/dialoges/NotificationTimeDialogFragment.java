package com.launcher.mummu.cabclient.dialoges;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.launcher.mummu.cabclient.R;
import com.launcher.mummu.cabclient.storage.CabStorageUtil;

/**
 * Created by muhammed on 2/1/2017.
 */

public class NotificationTimeDialogFragment extends AppCompatDialogFragment implements RadioGroup.OnCheckedChangeListener {
    public static final long ONE_KILOMTER = 1000;
    public static final long TWO_KILOMTER = 2000;
    public static final long THREE_KILOMTER = 3000;
    private TextView buttonTextView;
    private RadioGroup mRadioGroup;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(STYLE_NO_FRAME, R.style.transparent_dialog_borderless);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notification_time_dialog_fragment, container, false);
        buttonTextView = (TextView) view.findViewById(R.id.buttonTextView);
        mRadioGroup = (RadioGroup) view.findViewById(R.id.radios);

        buttonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                CabStorageUtil.storeDialogPref(getContext(), true);
                CabStorageUtil.storeDialogTime(getContext(), System.currentTimeMillis());
            }
        });
        long notificationKilometerRange = CabStorageUtil.getNotificationKilometerRange(getContext());
        if (notificationKilometerRange == 0l) {
            mRadioGroup.check(R.id.oneKmradioButton);
            onCheckedChanged(mRadioGroup, R.id.oneKmradioButton);
        } else if (notificationKilometerRange == ONE_KILOMTER) {
            onCheckedChanged(mRadioGroup, R.id.oneKmradioButton);
            mRadioGroup.check(R.id.oneKmradioButton);
        } else if (notificationKilometerRange == TWO_KILOMTER) {
            onCheckedChanged(mRadioGroup, R.id.twoKmRadioButton);
            mRadioGroup.check(R.id.twoKmRadioButton);
        } else {
            onCheckedChanged(mRadioGroup, R.id.threeKmRadioButton);
            mRadioGroup.check(R.id.threeKmRadioButton);
        }

        mRadioGroup.setOnCheckedChangeListener(this);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);

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
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.oneKmradioButton:
                CabStorageUtil.setNotificationKilometerRange(getContext(), ONE_KILOMTER);
                break;
            case R.id.twoKmRadioButton:
                CabStorageUtil.setNotificationKilometerRange(getContext(), TWO_KILOMTER);
                break;
            case R.id.threeKmRadioButton:
                CabStorageUtil.setNotificationKilometerRange(getContext(), THREE_KILOMTER);
                break;
        }
    }
}
