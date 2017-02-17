package com.launcher.mummu.cabclient.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.launcher.mummu.cabclient.CabUserApplication;
import com.launcher.mummu.cabclient.utils.FirebaseUtil;

/**
 * Created by muhammed on 2/17/2017.
 */

public class FirebaseService extends Service implements ChildEventListener, ValueEventListener {
    private final IBinder mBinder = new MyBinder();
    private OnCloudValueChangeListener listener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FirebaseDatabase firebaseInstance = CabUserApplication.getFirebaseInstance();
        DatabaseReference referenceFromUrl = firebaseInstance.getReferenceFromUrl(FirebaseUtil.FIREBASE_URL);
        referenceFromUrl.child(FirebaseUtil.MAIN_TAG).child(FirebaseUtil.LOCATION_TAG).addValueEventListener(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {


    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (listener != null) {
            listener.onValueChanged(dataSnapshot);
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    public void setOnCloudValueChangeListener(OnCloudValueChangeListener listener) {
        this.listener = listener;
    }


    public interface OnCloudValueChangeListener {
        void onValueChanged(DataSnapshot dataSnapshot);
    }

    public class MyBinder extends Binder {
        public FirebaseService getInstance() {
            return FirebaseService.this;
        }
    }

}
