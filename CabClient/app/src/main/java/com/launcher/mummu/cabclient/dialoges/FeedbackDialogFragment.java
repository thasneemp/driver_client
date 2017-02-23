package com.launcher.mummu.cabclient.dialoges;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.launcher.mummu.cabclient.R;
import com.launcher.mummu.cabclient.storage.CabStorageUtil;
import com.launcher.mummu.cabclient.storage.FirebaseStorage;

/**
 * Created by muhammed on 2/1/2017.
 */

public class FeedbackDialogFragment extends AppCompatDialogFragment {
    private String imageUrl = "";
    private String messageText = "";
    private String buttonText = "";
    private EditText mFeedbackEditText;

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
        View view = inflater.inflate(R.layout.feedback_dialog_fragment, container, false);
        imageViewUrl = (ImageView) view.findViewById(R.id.relativeLayout);
        messageTextView = (TextView) view.findViewById(R.id.messageTextView);
        buttonTextView = (TextView) view.findViewById(R.id.buttonTextView);
        mFeedbackEditText = (EditText) view.findViewById(R.id.editText);

        buttonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseStorage.pushUserComments(CabStorageUtil.getUUId(getContext()), mFeedbackEditText.getText().toString());
                dismiss();
            }
        });

        return view;
    }

    private String getVersion() {
        PackageManager manager = getContext().getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(
                    getContext().getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
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
