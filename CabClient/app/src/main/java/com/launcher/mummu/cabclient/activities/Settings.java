package com.launcher.mummu.cabclient.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.launcher.mummu.cabclient.R;
import com.launcher.mummu.cabclient.dialoges.AboutDialogFragment;
import com.launcher.mummu.cabclient.dialoges.FeedbackDialogFragment;
import com.launcher.mummu.cabclient.storage.CabStorageUtil;
import com.launcher.mummu.cabclient.storage.FirebaseStorage;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by muhammed on 2/20/2017.
 */

public class Settings extends Container implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private Toolbar mToolbar;
    private FirebaseAuth firebaseAuth;
    private CircleImageView mCircleImageView;
    private TextView mNameTextView;
    private TextView morningNotificationtext;
    private TextView eveningNotificationtext;
    private Button mLogoutButton;
    private SwitchCompat mSwitchCompat;
    private SwitchCompat mSwitchMorning;
    private SwitchCompat mSwitchEvening;
    private CardView mAboutCardView;
    private CardView mFeedbackCardView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mCircleImageView = (CircleImageView) findViewById(R.id.profile_image);
        mNameTextView = (TextView) findViewById(R.id.usernameTextview);
        morningNotificationtext = (TextView) findViewById(R.id.morningNotificationtext);
        eveningNotificationtext = (TextView) findViewById(R.id.eveningNotificationtext);
        mLogoutButton = (Button) findViewById(R.id.logout_button);
        mSwitchCompat = (SwitchCompat) findViewById(R.id.switchCopmat);
        mSwitchMorning = (SwitchCompat) findViewById(R.id.switchCopmatMorning);
        mSwitchEvening = (SwitchCompat) findViewById(R.id.switchCopmatEvening);
        mAboutCardView = (CardView) findViewById(R.id.card_view_about);
        mFeedbackCardView = (CardView) findViewById(R.id.card_view_feedback);
        mLogoutButton.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();

        mSwitchMorning.setChecked(CabStorageUtil.isMorningChecked(this));
        mSwitchEvening.setChecked(CabStorageUtil.isEveningChecked(this));


        mSwitchCompat.setChecked(CabStorageUtil.isNotificationOn(this));
        mSwitchMorning.setEnabled(CabStorageUtil.isNotificationOn(this));
        mSwitchEvening.setEnabled(CabStorageUtil.isNotificationOn(this));


        mSwitchCompat.setOnCheckedChangeListener(this);
        mSwitchMorning.setOnCheckedChangeListener(this);
        mSwitchEvening.setOnCheckedChangeListener(this);

        mAboutCardView.setOnClickListener(this);
        mFeedbackCardView.setOnClickListener(this);

        FirebaseUser user = firebaseAuth.getCurrentUser();


        //Load user profile
        if (user != null) {
            Log.d("PHOTO", "setUI: " + user.getPhotoUrl());
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.person)
                    .error(R.drawable.person)
                    .dontAnimate()
                    .into(mCircleImageView);
            mNameTextView.setText(user.getDisplayName());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logout_button:
                showConfirmationDialog();
                break;
            case R.id.card_view_about:
                AboutDialogFragment aboutDialogFragment = new AboutDialogFragment();
                aboutDialogFragment.show(getSupportFragmentManager(), aboutDialogFragment.getClass().getName());
                break;
            case R.id.card_view_feedback:
                FeedbackDialogFragment feedbackDialogFragment = new FeedbackDialogFragment();
                feedbackDialogFragment.show(getSupportFragmentManager(), feedbackDialogFragment.getClass().getName());

                break;
        }
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to logout now?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseStorage.updateLoginInfo(CabStorageUtil.getUUId(Settings.this), false);
                logOut();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        CabStorageUtil.clearAll(this);
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
        super.onBackPressed();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switchCopmat:
                CabStorageUtil.setNotificationStatus(this, isChecked);
                mSwitchMorning.setEnabled(isChecked);
                mSwitchEvening.setEnabled(isChecked);
                break;
            case R.id.switchCopmatMorning:
                CabStorageUtil.setMorningNotificationStatus(this, isChecked);
                break;
            case R.id.switchCopmatEvening:
                CabStorageUtil.setEveningNotificationStatus(this, isChecked);
                break;
        }

    }
}
