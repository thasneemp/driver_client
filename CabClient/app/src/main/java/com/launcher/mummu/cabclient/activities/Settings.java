package com.launcher.mummu.cabclient.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.launcher.mummu.cabclient.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by muhammed on 2/20/2017.
 */

public class Settings extends Container {
    private Toolbar mToolbar;
    private FirebaseAuth firebaseAuth;
    private CircleImageView mCircleImageView;
    private TextView mNameTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mCircleImageView = (CircleImageView) findViewById(R.id.profile_image);
        mNameTextView = (TextView) findViewById(R.id.usernameTextview);
        firebaseAuth = FirebaseAuth.getInstance();

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
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
