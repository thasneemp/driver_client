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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.launcher.mummu.cabclient.R;
import com.launcher.mummu.cabclient.storage.CabStorageUtil;

/**
 * Created by muhammed on 2/1/2017.
 */

public class PromotionDialogFragment extends AppCompatDialogFragment {
    private String imageUrl = "";
    private String messageText = "";
    private String buttonText = "";

    private ImageView imageViewUrl;
    private TextView messageTextView;
    private TextView buttonTextView;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(STYLE_NO_FRAME, R.style.transparent_dialog_borderless);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.prmo_dialog_fragment, container, false);
        imageViewUrl = (ImageView) view.findViewById(R.id.relativeLayout);
        messageTextView = (TextView) view.findViewById(R.id.messageTextView);
        buttonTextView = (TextView) view.findViewById(R.id.buttonTextView);

        buttonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                CabStorageUtil.storeDialogPref(getContext(), true);
                CabStorageUtil.storeDialogTime(getContext(), System.currentTimeMillis());
            }
        });
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.promo_cab)
                .error(R.drawable.promo_cab)
                .dontAnimate()
                .into(imageViewUrl);
        buttonTextView.setText(getButtonText());
        messageTextView.setText("# " + getMessageText());
        return view;
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
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
    }
}
