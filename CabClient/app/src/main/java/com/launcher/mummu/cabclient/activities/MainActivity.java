package com.launcher.mummu.cabclient.activities;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.launcher.mummu.cabclient.R;
import com.launcher.mummu.cabclient.dialoges.PromotionDialogFragment;
import com.launcher.mummu.cabclient.dialoges.TimeDialogFragment;
import com.launcher.mummu.cabclient.dialoges.WelcomeDialogFragment;
import com.launcher.mummu.cabclient.models.LocationModel;
import com.launcher.mummu.cabclient.models.MessageModel;
import com.launcher.mummu.cabclient.models.distanceapi.Distance;
import com.launcher.mummu.cabclient.models.distanceapi.DistanceMain;
import com.launcher.mummu.cabclient.models.distanceapi.Duration;
import com.launcher.mummu.cabclient.models.distanceapi.Elements;
import com.launcher.mummu.cabclient.models.distanceapi.Rows;
import com.launcher.mummu.cabclient.service.FirebaseService;
import com.launcher.mummu.cabclient.service.GPSService;
import com.launcher.mummu.cabclient.storage.CabStorageUtil;
import com.launcher.mummu.cabclient.storage.FirebaseStorage;
import com.launcher.mummu.cabclient.storage.PreferenceHelperEvening;
import com.launcher.mummu.cabclient.storage.PreferenceHelperMorning;
import com.launcher.mummu.cabclient.utils.NetworkManager;
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

public class MainActivity extends Container implements OnMapReadyCallback, FirebaseService.OnCloudValueChangeListener, View.OnClickListener, GPSService.OnLocationChange {
    private static final int PERMISSION_REQUEST = 100;
    private static final double LAT_START = 10.009385;
    private static final double LONG_START = 76.361632;
    private static final double LAT_STOP1 = 10.011730;
    private static final double LONG_STOP1 = 76.322886;
    private static final double LAT_STOP2 = 10.002676;
    private static final double LONG_STOP2 = 76.306478;
    private static final int REQUEST_SETTINGS = 12458;
    private static final float BEARING_OFFSET = 20;
    private static final int ANIMATE_SPEEED_TURN = 1000;

    private static final int TIME_INTERVAL = 20000; // # milliseconds, desired time passed between two back presses.
    private static final String URL_DISTANCE = "https://maps.googleapis.com/maps/api/distancematrix/json?";
    private static final String URL_BALANCE = "mode=driving&language=en&key=AIzaSyBkBF07tPOxZVC9c7PhKPGGrWPn3z0_QO8";
    ArrayList<LatLng> latLngs = new ArrayList<>();
    Marker marker = null;
    private long mBackPressed;
    private Snackbar snackbar;
    private Marker userMarker;
    private MarkerOptions userStopMarkerOptions;
    private FirebaseAuth mAuth;
    private ImageButton mBusButton = null;
    private ImageButton mTerainSatButton = null;
    private ImageButton mTimeButton = null;
    private ImageButton mStopButton = null;
    private ImageButton mMyLocationButton = null;
    private CircleImageView mCircleImageView = null;
    private MapFragment mMapFragment;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private boolean isMarkerRotating;
    private MarkerOptions icon;
    private FrameLayout frameLayout;
    private TextView mInfoTextView;
    private Location myLocation;
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

    private ServiceConnection gpsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GPSService.MyBinder myBinder = (GPSService.MyBinder) service;
            GPSService gpsService = myBinder.getService();
            gpsService.setOnLocationListener(MainActivity.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onResume() {

        Intent service = new Intent(this, FirebaseService.class);
        startService(service);
        Intent gpsService = new Intent(this, GPSService.class);
        startService(gpsService);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Utils.showGPSDisabledAlertToUser(this);
        } else {
            Intent intent = new Intent(this, GPSService.class);
            bindService(intent, gpsServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
        Intent intent = new Intent(this, FirebaseService.class);
        bindService(intent, serviceConnection,
                Context.BIND_AUTO_CREATE);
        addUserStop();
        EventBus.getDefault().register(this);

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
            unbindService(gpsServiceConnection);
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
        mMyLocationButton = (ImageButton) findViewById(R.id.myLocationbutton);
        mStopButton = (ImageButton) findViewById(R.id.stopsbutton);
        frameLayout = (FrameLayout) findViewById(R.id.container);
        mCircleImageView = (CircleImageView) findViewById(R.id.profile_image);
        mInfoTextView = (TextView) findViewById(R.id.kilometerDisplayTextView);

        mBusButton.setOnClickListener(this);
        mMyLocationButton.setOnClickListener(this);
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

// show welcome dialog
        if (!CabStorageUtil.isFirstTime(this)) {
            WelcomeDialogFragment dialogFragment = new WelcomeDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), dialogFragment.getClass().getName());

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.setTrafficEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        addStartPosition();
    }

    private void addStartPosition() {
        MarkerOptions claysysMarkerOptions = new MarkerOptions().position(new LatLng(LAT_START, LONG_START)).title(getString(R.string.claysys_stop)).icon(BitmapDescriptorFactory.fromResource(R.drawable.claysys_stop));
        MarkerOptions stop1MarkerOptions = new MarkerOptions().position(new LatLng(LAT_STOP1, LONG_STOP1)).title(getString(R.string.chembumukku_stop)).icon(BitmapDescriptorFactory.fromResource(R.drawable.stops));
        MarkerOptions stop2MarkerOptions = new MarkerOptions().position(new LatLng(LAT_STOP2, LONG_STOP2)).title(getString(R.string.palarivattom_stop)).icon(BitmapDescriptorFactory.fromResource(R.drawable.stops));
        addUserStop();
        googleMap.addMarker(claysysMarkerOptions);
        googleMap.addMarker(stop1MarkerOptions);
        googleMap.addMarker(stop2MarkerOptions);


    }

    private void addUserStop() {
        final LatLng latLng = CabStorageUtil.getLocationLatLng(this);
        if (latLng != null) {
            final String location = CabStorageUtil.getLocation(this, CabStorageUtil.LOCATION_NAME);
            Glide.with(this).load(CabStorageUtil.getUserProfile(this)).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    if (userStopMarkerOptions == null) {
                        userStopMarkerOptions = new MarkerOptions().position(latLng).title(location).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        userStopMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(UIUtil.getCircleBitmap(resource, MainActivity.this)));
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

                @Override
                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                    if (userStopMarkerOptions == null) {
                        userStopMarkerOptions = new MarkerOptions().position(latLng).title(location).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        userStopMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker());
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
                    super.onLoadFailed(e, errorDrawable);
                }
            });

        }
    }

    private void enableLocation(boolean value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
                return;
            } else {
                googleMap.setMyLocationEnabled(value);
            }
        } else {
            googleMap.setMyLocationEnabled(value);
        }

    }

    @Override
    public void onValueChanged(DataSnapshot dataSnapshot) {
        LocationModel value = dataSnapshot.getValue(LocationModel.class);
        LatLng latLng = new LatLng(value.getLat(), value.getLonge());
        if (latLngs.size() > 1) {
            latLngs.remove(0);
            latLngs.add(latLng);
        } else {
            latLngs.add(latLng);
            moveToBusLocation(latLng);
        }

        if (icon == null) {
            icon = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_logo)).title("Cab");
            marker = googleMap.addMarker(icon);
        }
        if (latLngs.size() > 1) {
            animateMarker(latLngs.get(1), false, marker, value.getBearing());
            if (myLocation != null) {
                showText(myLocation, convertLatLngToLocation(latLngs.get(1)));
            }

        }

    }

    private void showText(Location fromLatLng, Location toLatLng) {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            return;
        } else {
            NetworkManager.getInstance().call(URL_DISTANCE + "origins=" +
                    fromLatLng.getLatitude() + "," + fromLatLng.getLongitude() +
                    "&" + "destinations=" + toLatLng.getLatitude() + ","
                    + toLatLng.getLongitude() + "&"
                    + URL_BALANCE, this).setOnApiReslutCallback(new NetworkManager.OnApiResult() {
                @Override
                public void onResult(Exception e, JsonObject result) {
                    if (result != null) {
                        Gson gson = new Gson();
                        DistanceMain distanceMain = gson.fromJson(result.toString(), DistanceMain.class);
                        Rows[] rows = distanceMain.getRows();
                        if (rows != null && rows.length > 0) {
                            Rows row = rows[0];
                            Elements[] elements = row.getElements();
                            if (elements.length > 0) {
                                Distance distance = elements[0].getDistance();
                                Duration duration = elements[0].getDuration();
                                mInfoTextView.setVisibility(View.VISIBLE);
                                mInfoTextView.setText("Cab is " + distance.getText() + " away from you,\nit will take approximately " +
                                        duration.getText() + ".\nNow at " + distanceMain.getDestination_addresses()[0].split(",")[0]);
                                mBackPressed = System.currentTimeMillis();
                            }
                        }
                    }

                }
            });
        }


//        try {
//            float v = fromLatLng.distanceTo(toLatLng);
//            if (v > 1000) {
//                float kilometer = v / 1000;
//                mInfoTextView.setText("Cab is " + new DecimalFormat("##.##").format(kilometer) + "Km away from you");
//            } else if (v < 10) {
//                mInfoTextView.setText("Cab is near to you");
//            } else {
//                mInfoTextView.setText("Cab is " + (int) v + "m away from you");
//            }
//
//        } catch (Exception e) {
//            FirebaseCrash.report(e);
//        }

    }

    @Override
    public void onLocationNearToYou(float distance) {
        Fragment fragment = null;
        if (CabStorageUtil.isNotificationOn(this)) {
            if (CabStorageUtil.isTodayMorningShow(this) || CabStorageUtil.isTodayEveningShow(this)) {
                PreferenceHelperMorning.setRemindInterval(this);
                PreferenceHelperEvening.setRemindInterval(this);
                long kilometerRange = CabStorageUtil.getNotificationKilometerRange(this);
                PromotionDialogFragment promotionDialogFragment = (PromotionDialogFragment) getSupportFragmentManager().findFragmentByTag(PromotionDialogFragment.class.getName());
                if (promotionDialogFragment == null && fragment == null) {
                    MessageModel model = new MessageModel();
                    model.setButtonText("Dismiss");
                    model.setImageUrl("");
                    model.setMessage("Hello your cab is near to " + (kilometerRange == 0l ? (1000 / 1000) : (kilometerRange / 1000)) + "Km; Please be available");
                    showDialog(model);
                }
            }
        }
    }

    public void animateMarker(final LatLng toPosition, final boolean hideMarke, final Marker m, final float value) {
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
//                    double v = bearingBetweenLocations(latLngs.get(0), latLngs.get(1));
//                    Log.d("BEARING", "run: " + v);

                    CameraPosition cameraPosition =
                            new CameraPosition.Builder()
                                    .target(latLngs.get(1)) // changed this...
                                    .bearing((value))
                                    .zoom(googleMap.getCameraPosition().zoom)
                                    .build();
                    googleMap.animateCamera(
                            CameraUpdateFactory.newCameraPosition(cameraPosition),
                            ANIMATE_SPEEED_TURN,
                            null);

                }
            }
        });
    }

    private Location convertLatLngToLocation(LatLng latLng) {
        Location loc = new Location("someLoc");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        return loc;
    }

//    private double bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {
//        Location beginL = convertLatLngToLocation(latLng1);
//        Location endL = convertLatLngToLocation(latLng2);
////        double PI = 3.14159;
////        double lat1 = latLng1.latitude * PI / 180;
////        double long1 = latLng1.longitude * PI / 180;
////        double lat2 = latLng2.latitude * PI / 180;
////        double long2 = latLng2.longitude * PI / 180;
////
////        double dLon = (long2 - long1);
////
////        double y = Math.sin(dLon) * Math.cos(lat2);
////        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
////                * Math.cos(lat2) * Math.cos(dLon);
////
////        double brng = Math.atan2(y, x);
////
////        brng = Math.toDegrees(brng);
////        brng = (brng + 360) % 360;
//
//        return beginL.bearingTo(endL);
//    }

//    private void rotateMarker(final Marker marker, final float toRotation) {
//        if (!isMarkerRotating) {
//            final Handler handler = new Handler();
//            final long start = SystemClock.uptimeMillis();
//            final float startRotation = marker.getRotation();
//            final long duration = 1000;
//
//            final Interpolator interpolator = new LinearInterpolator();
//
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    isMarkerRotating = true;
//
//                    long elapsed = SystemClock.uptimeMillis() - start;
//                    float t = interpolator.getInterpolation((float) elapsed / duration);
//
//                    float rot = t * toRotation + (1 - t) * startRotation;
//
//                    marker.setRotation(-rot > 180 ? rot / 2 : rot);
//                    if (t < 1.0) {
//                        // Post again 16ms later.
//                        handler.postDelayed(this, 16);
//                    } else {
//                        isMarkerRotating = false;
//                    }
//                }
//            });
//        }
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.busbutton:
                if (latLngs.size() > 0) {
                    moveToBusLocation(latLngs.get(0));
                } else {
                    Toast.makeText(this, "Please wait collecting information", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tersatbutton:
                if (mTerainSatButton.getTag() != null && ((String) mTerainSatButton.getTag()).equalsIgnoreCase("ter")) {
                    mTerainSatButton.setImageResource(R.drawable.terrain);
                    mTerainSatButton.setTag("sat");
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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
            default:
                if (mMyLocationButton.getTag() != null && ((String) mMyLocationButton.getTag()).equalsIgnoreCase("disable")) {
                    mMyLocationButton.setImageResource(R.drawable.gps_off);
                    mMyLocationButton.setTag("enable");
                    enableLocation(false);
                } else {
                    mMyLocationButton.setImageResource(R.drawable.gps);
                    mMyLocationButton.setTag("disable");
                    enableLocation(true);
                }
                break;

        }
    }

    private void moveToBusLocation(LatLng latLng) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        googleMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length >= 3) {
            enableLocation(true);
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

    @Override
    public void onLocationChanged(Location location) {
        this.myLocation = location;
    }
}
