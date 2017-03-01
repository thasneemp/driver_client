package com.launcher.mummu.cabclient.activities;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.launcher.mummu.cabclient.R;
import com.launcher.mummu.cabclient.dialoges.NotificationTimeDialogFragment;
import com.launcher.mummu.cabclient.storage.CabStorageUtil;
import com.launcher.mummu.cabclient.utils.UIUtil;

/**
 * Created by muhammed on 2/20/2017.
 */

public class StopSelectionActivity extends Container implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private static final int REQUEST_PERMISSION = 10214;
    private MapFragment mMapFragment;
    private GoogleMap googleMap;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops_nearby_activity);
        setUI();
    }

    private void setUI() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mMapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.stopsContainer, mMapFragment);
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        enableLocation();
        googleMap.setOnMapLongClickListener(this);
        LatLng latLng = CabStorageUtil.getLocationLatLng(this);
        if (latLng != null) {
            addMarker(latLng);
        }

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                CabStorageUtil.storeLocation(StopSelectionActivity.this, CabStorageUtil.LOCATION_LAT, marker.getPosition().latitude + "");
                CabStorageUtil.storeLocation(StopSelectionActivity.this, CabStorageUtil.LOCATION_LONG, marker.getPosition().longitude + "");
                showMarkerDialoge(marker);
                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length >= 2) {
            enableLocation();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void enableLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION);
                return;
            } else {
                googleMap.setMyLocationEnabled(true);
            }
        } else {
            googleMap.setMyLocationEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.notificationTime:
                NotificationTimeDialogFragment fragment = new NotificationTimeDialogFragment();
                fragment.show(getSupportFragmentManager(), fragment.getClass().getName());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        googleMap.clear();
        vibrate(100);
        addMarker(latLng);


    }

    private void addMarker(LatLng latLng) {
        final MarkerOptions icon = new MarkerOptions().position(latLng).draggable(false);
        Glide.with(this).load(CabStorageUtil.getUserProfile(this)).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                icon.icon(BitmapDescriptorFactory.fromBitmap(UIUtil.getCircleBitmap(resource, StopSelectionActivity.this)));
                googleMap.addMarker(icon);
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                icon.icon(BitmapDescriptorFactory.defaultMarker());
                googleMap.addMarker(icon);
                super.onLoadFailed(e, errorDrawable);
            }
        });


    }


    private void showMarkerDialoge(final Marker marker) {
        String title = CabStorageUtil.getLocation(this, CabStorageUtil.LOCATION_NAME);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNegativeButton("Cancel", null);
        final EditText editText = new EditText(this);
        if (title != null && title.length() > 0) {
            editText.setText(title);
        }
        builder.setView(editText);
        builder.setTitle("Title");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editText.getText().toString().length() > 0) {
                    CabStorageUtil.storeLocation(StopSelectionActivity.this, CabStorageUtil.LOCATION_NAME, editText.getText().toString());

                }
            }
        });
        builder.create().show();
    }

    private void vibrate(long i) {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stop_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

}
