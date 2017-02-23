package com.launcher.mummu.cabclient.activities;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.launcher.mummu.cabclient.R;
import com.launcher.mummu.cabclient.dialoges.PromotionDialogFragment;
import com.launcher.mummu.cabclient.dialoges.TimeDialogFragment;
import com.launcher.mummu.cabclient.models.MessageModel;
import com.launcher.mummu.cabclient.service.FirebaseService;
import com.launcher.mummu.cabclient.storage.CabStorageUtil;
import com.launcher.mummu.cabclient.storage.FirebaseStorage;
import com.launcher.mummu.cabclient.storage.PreferenceHelperEvening;
import com.launcher.mummu.cabclient.storage.PreferenceHelperMorning;
import com.launcher.mummu.cabclient.utils.FirebaseUtil;
import com.launcher.mummu.cabclient.utils.UIUtil;
import com.launcher.mummu.cabclient.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by muhammed on 2/17/2017.
 */

public class MainActivity extends Container implements OnMapReadyCallback, FirebaseService.OnCloudValueChangeListener, View.OnClickListener {
    private static final int PERMISSION_REQUEST = 100;
    private static final double LAT_START = 10.009167;
    private static final double LONG_START = 76.362869;
    private static final double LAT_STOP1 = 10.011730;
    private static final double LONG_STOP1 = 76.322886;
    private static final double LAT_STOP2 = 10.002676;
    private static final double LONG_STOP2 = 76.306478;
    private static final int REQUEST_SETTINGS = 12458;
    ArrayList<LatLng> latLngs = new ArrayList<>();
    Marker marker = null;
    private Snackbar snackbar;
    private Marker userMarker;
    private MarkerOptions userStopMarkerOptions;
    private FirebaseAuth mAuth;
    private ImageButton mBusButton = null;
    private ImageButton mTerainSatButton = null;
    private ImageButton mTimeButton = null;
    private ImageButton mStopButton = null;
    private CircleImageView mCircleImageView = null;
    private MapFragment mMapFragment;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private boolean isMarkerRotating;
    private MarkerOptions icon;
    private FrameLayout frameLayout;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FirebaseService.MyBinder firebaseService = (FirebaseService.MyBinder) service;
            FirebaseService instance = firebaseService.getInstance();
            instance.setOnCloudValueChangeListener(MainActivity.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

//    private ServiceConnection gpsServiceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            GPSService.MyBinder myBinder = (GPSService.MyBinder) service;
//            GPSService gpsService = myBinder.getService();
//            gpsService.setOnLocationListener(MainActivity.this);
//
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//
//        }
//    };

    @Override
    protected void onResume() {

        Intent service = new Intent(this, FirebaseService.class);
        startService(service);
//        Intent gpsIntent = new Intent(this, GPSService.class);
//        startService(gpsIntent);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Utils.showGPSDisabledAlertToUser(this);
        } else {
            Intent intent = new Intent(this, FirebaseService.class);
            bindService(intent, serviceConnection,
                    Context.BIND_AUTO_CREATE);
//            Intent gspService = new Intent(this, GPSService.class);
//            bindService(gspService, gpsServiceConnection,
//                    Context.BIND_AUTO_CREATE);
        }
        addUserStop();
        EventBus.getDefault().register(this);
//        CabStorageUtil.storeDialogPref(this, false);


        //Update user last seen status

        FirebaseStorage.updateLastSeen(new Date(), CabStorageUtil.getUUId(this));
        super.onResume();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUI();
    }


    @Override
    protected void onPause() {
        try {
            unbindService(serviceConnection);
//            unbindService(gpsServiceConnection);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageModel messageModel) {
        showDialog(messageModel);
    }

    private Fragment showDialog(MessageModel messageModel) {
        PromotionDialogFragment fragment = new PromotionDialogFragment();
//        TimeDialogFragment fragment = new TimeDialogFragment();
        fragment.setImageUrl(messageModel.getImageUrl());
        fragment.setMessageText(messageModel.getMessage());
        fragment.setButtonText(messageModel.getButtonText());
        fragment.show(getSupportFragmentManager(), fragment.getClass().getName());
        return fragment;
    }

    private void setUI() {
        mMapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container, mMapFragment);
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);
        mBusButton = (ImageButton) findViewById(R.id.busbutton);
        mTerainSatButton = (ImageButton) findViewById(R.id.tersatbutton);
        mTimeButton = (ImageButton) findViewById(R.id.timebutton);
        mStopButton = (ImageButton) findViewById(R.id.stopsbutton);
        frameLayout = (FrameLayout) findViewById(R.id.container);
        mCircleImageView = (CircleImageView) findViewById(R.id.profile_image);

        mBusButton.setOnClickListener(this);
        mTerainSatButton.setOnClickListener(this);
        mTimeButton.setOnClickListener(this);
        mCircleImageView.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();


        //Load user profile
        if (user != null) {
            Log.d("PHOTO", "setUI: " + user.getPhotoUrl());
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.person)
                    .error(R.drawable.person)
                    .dontAnimate()
                    .into(mCircleImageView);
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        enableLocation();
        addStartPosition();
    }

    private void addStartPosition() {
        MarkerOptions claysysMarkerOptions = new MarkerOptions().position(new LatLng(LAT_START, LONG_START)).title("Claysys").icon(BitmapDescriptorFactory.fromResource(R.drawable.claysys_stop));
        MarkerOptions stop1MarkerOptions = new MarkerOptions().position(new LatLng(LAT_STOP1, LONG_STOP1)).title("Chembumukku").icon(BitmapDescriptorFactory.fromResource(R.drawable.stops));
        MarkerOptions stop2MarkerOptions = new MarkerOptions().position(new LatLng(LAT_STOP2, LONG_STOP2)).title("Palarivattom").icon(BitmapDescriptorFactory.fromResource(R.drawable.stops));
        addUserStop();
        googleMap.addMarker(claysysMarkerOptions);
        googleMap.addMarker(stop1MarkerOptions);
        googleMap.addMarker(stop2MarkerOptions);


    }

    private void addUserStop() {
        LatLng latLng = CabStorageUtil.getLocationLatLng(this);
        if (latLng != null) {
            String location = CabStorageUtil.getLocation(this, CabStorageUtil.LOCATION_NAME);
            if (userStopMarkerOptions == null) {
                userStopMarkerOptions = new MarkerOptions().position(latLng).title(location).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            } else {
                if (userMarker != null) {
                    userMarker.setPosition(latLng);
                }
            }
            if (googleMap != null) {
                if (userMarker == null) {
                    userMarker = googleMap.addMarker(userStopMarkerOptions);
                }

            }
        }
    }

    private void enableLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
                return;
            } else {
                googleMap.setMyLocationEnabled(true);
            }
        } else {
            googleMap.setMyLocationEnabled(true);
        }

    }

    @Override
    public void onValueChanged(DataSnapshot dataSnapshot) {
        DataSnapshot latitude = dataSnapshot.child(FirebaseUtil.LATITUDE_TAG);
        DataSnapshot longitude = dataSnapshot.child(FirebaseUtil.LONGITUDE_TAG);
        double latiudeLn = (double) latitude.getValue();
        double longitudeLn = (double) longitude.getValue();
        LatLng latLng = new LatLng(latiudeLn, longitudeLn);

        if (latLngs.size() > 1) {
            latLngs.remove(0);
            latLngs.add(latLng);
        } else {
            latLngs.add(latLng);
        }

        if (icon == null) {
            icon = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)).title("Cab");
            marker = googleMap.addMarker(icon);
        }
        if (latLngs.size() > 1) {
            animateMarker(latLngs.get(1), false, marker);
//           marker.setPosition(latLngs.get(1));
        }

    }

    @Override
    public void onLocationNearToYou(float distance) {
        Fragment fragment = null;
        if (CabStorageUtil.isNotificationOn(this)) {
            if (CabStorageUtil.isTodayMorningShow(this) || CabStorageUtil.isTodayEveningShow(this)) {
                PreferenceHelperMorning.setRemindInterval(this);
                PreferenceHelperEvening.setRemindInterval(this);
                PromotionDialogFragment promotionDialogFragment = (PromotionDialogFragment) getSupportFragmentManager().findFragmentByTag(PromotionDialogFragment.class.getName());
                if (promotionDialogFragment == null && fragment == null) {
                    MessageModel model = new MessageModel();
                    model.setButtonText("Dismiss");
                    model.setImageUrl("");
                    model.setMessage("Hello your cab is near to you; Please be available");
                    fragment = showDialog(model);
                }
            }
        }
    }

    public void animateMarker(final LatLng toPosition, final boolean hideMarke, final Marker m) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = googleMap.getProjection();
        Point startPoint = proj.toScreenLocation(m.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 1000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                m.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarke) {
                        m.setVisible(false);
                    } else {
                        m.setVisible(true);
                    }
                    double v = bearingBetweenLocations(latLngs.get(0), latLngs.get(1));
                    Log.d("BEARING", "run: " + v);
                    rotateMarker(marker, (float) v);

                }
            }
        });
    }

    private double bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }

    private void rotateMarker(final Marker marker, final float toRotation) {
        if (!isMarkerRotating) {
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final float startRotation = marker.getRotation();
            final long duration = 1000;

            final Interpolator interpolator = new LinearInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    isMarkerRotating = true;

                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed / duration);

                    float rot = t * toRotation + (1 - t) * startRotation;

                    marker.setRotation(-rot > 180 ? rot / 2 : rot);
                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    } else {
                        isMarkerRotating = false;
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.busbutton:
                if (latLngs.size() > 0) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLngs.get(0), 15);
                    googleMap.animateCamera(cameraUpdate);
                }
                break;
            case R.id.tersatbutton:
                if (mTerainSatButton.getTag() != null && ((String) mTerainSatButton.getTag()).equalsIgnoreCase("ter")) {
                    mTerainSatButton.setImageResource(R.drawable.terrain);
                    mTerainSatButton.setTag("sat");
                    googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                } else {
                    mTerainSatButton.setImageResource(R.drawable.satellite);
                    mTerainSatButton.setTag("ter");
                    googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }
                break;
            case R.id.profile_image:
                startActivityForResult(new Intent(this, Settings.class), REQUEST_SETTINGS);
                break;
            case R.id.timebutton:
                TimeDialogFragment fragment = new TimeDialogFragment();
                fragment.show(getSupportFragmentManager(), fragment.getClass().getName());
                break;
            case R.id.stopsbutton:
                startActivity(new Intent(this, StopSelectionActivity.class));
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length >= 3) {
            enableLocation();
        } else {
            Toast.makeText(this, "Cant run application without permission", Toast.LENGTH_SHORT).show();
            finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            switch (requestCode) {
                case REQUEST_SETTINGS:
                    finish();
                    break;
            }
        }
    }

    @Override
    public void onNetworkChanged(int status) {
        super.onNetworkChanged(status);
        switch (status) {
            case Container.NETWORK_DISCONNECTED:
                snackbar = UIUtil.showSnackBar(this, frameLayout, "Network disconnected");
                break;
            case Container.NETWORK_CONNECTED:
                if (snackbar != null) {
                    snackbar.dismiss();
                }
                break;
        }
    }

}
