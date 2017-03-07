package com.launcher.mummu.cabclient.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.launcher.mummu.cabclient.CabUserApplication;
import com.launcher.mummu.cabclient.models.MessageModel;
import com.launcher.mummu.cabclient.storage.CabStorageUtil;
import com.launcher.mummu.cabclient.storage.PreferenceHelperEvening;
import com.launcher.mummu.cabclient.storage.PreferenceHelperMorning;
import com.launcher.mummu.cabclient.utils.FirebaseUtil;
import com.launcher.mummu.cabclient.utils.NotificationUtils;
import com.launcher.mummu.cabclient.utils.UIUtil;

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
        try {
            if (listener != null) {
                listener.onValueChanged(dataSnapshot);
            }
//            DataSnapshot latitude = dataSnapshot.child(FirebaseUtil.LATITUDE_TAG);
//            DataSnapshot longitude = dataSnapshot.child(FirebaseUtil.LONGITUDE_TAG);
//            double latiudeLn = (double) latitude.getValue();
//            double longitudeLn = (double) longitude.getValue();
//            LatLng locationLatLng = CabStorageUtil.getLocationLatLng(this);
//            if (locationLatLng != null) {
//                float[] floats = new float[1];
//                Location.distanceBetween(locationLatLng.latitude, locationLatLng.longitude, latiudeLn, longitudeLn, floats);
//                long kilometerRange = CabStorageUtil.getNotificationKilometerRange(getApplicationContext());
//                if (floats[0] < (kilometerRange == 0l ? NotificationTimeDialogFragment.ONE_KILOMTER : kilometerRange)) {
//                    if (listener != null) {
//                        listener.onLocationNearToYou(floats[0]);
//                    }
////                    doBackCheck();
//                }
//
//            }
        } catch (Exception e) {
            FirebaseCrash.report(e);
//            doBackCheck();
        }
    }

    private void doBackCheck() {
        if (UIUtil.isAppIsInBackground(this)) {
            if (CabStorageUtil.isNotificationOn(this)) {
                if (CabStorageUtil.isTodayMorningShow(this) || CabStorageUtil.isTodayEveningShow(this)) {
                    long kilometerRange = CabStorageUtil.getNotificationKilometerRange(this);
                    PreferenceHelperMorning.setRemindInterval(this);
                    PreferenceHelperEvening.setRemindInterval(this);
                    MessageModel model = new MessageModel();
                    model.setMessage("Hello your cab is near to " + (kilometerRange == 0l ? (1000 / 1000) : (kilometerRange / 1000)) + "Km; Please be available");

                    // STORE ONE PREFERENCE TIME

                    CabStorageUtil.storeDialogPref(this, true);
                    CabStorageUtil.storeDialogTime(this, System.currentTimeMillis());

                    NotificationUtils.sendNotification(this, "test", model);
                }
            }
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

        void onLocationNearToYou(float distance);
    }

    public class MyBinder extends Binder {
        public FirebaseService getInstance() {
            return FirebaseService.this;
        }
    }

}
