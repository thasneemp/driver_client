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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;

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
import com.google.firebase.database.DataSnapshot;
import com.launcher.mummu.cabclient.R;
import com.launcher.mummu.cabclient.service.FirebaseService;
import com.launcher.mummu.cabclient.utils.FirebaseUtil;
import com.launcher.mummu.cabclient.utils.Utils;

import java.util.ArrayList;

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
    ArrayList<LatLng> latLngs = new ArrayList<>();
    Marker marker = null;
    private ImageButton mBusButton = null;
    private ImageButton mTerainSatButton = null;
    private ImageButton mTimeButton = null;
    private MapFragment mMapFragment;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private boolean isMarkerRotating;
    private MarkerOptions icon;
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


    @Override
    protected void onResume() {
        Intent service = new Intent(this, FirebaseService.class);
        startService(service);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Utils.showGPSDisabledAlertToUser(this);
        } else {
            Intent intent = new Intent(this, FirebaseService.class);
            bindService(intent, serviceConnection,
                    Context.BIND_AUTO_CREATE);
        }
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
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        super.onPause();
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

        mBusButton.setOnClickListener(this);
        mTerainSatButton.setOnClickListener(this);
        mTimeButton.setOnClickListener(this);
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
        googleMap.addMarker(claysysMarkerOptions);
        googleMap.addMarker(stop1MarkerOptions);
        googleMap.addMarker(stop2MarkerOptions);

    }

    private void enableLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
                return;
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST);
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
        }

        if (latLngs.size() > 1) {
            double v = bearingBetweenLocations(latLngs.get(0), latLngs.get(1));
            rotateMarker(marker, (float) v);
        }


    }

    public void animateMarker(final LatLng toPosition, final boolean hideMarke, final Marker m) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = googleMap.getProjection();
        Point startPoint = proj.toScreenLocation(m.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 3000;

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

            case R.id.timebutton:
                //TODO Bus timings
                break;

        }
    }
}
