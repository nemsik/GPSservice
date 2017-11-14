package com.example.bartek.gpsservice;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by bartek on 09.11.2017.
 */

public class MyService extends Service implements GpsStatus.Listener, ActivityCompat.OnRequestPermissionsResultCallback{
    public static final String
            ACTION_LOCATION_BROADCAST = "event-name",
            EXTRA_ISGPSFIX = "is_gps_fix",
            EXTRA_GPSSTATUS = "gps_status",
            EXTRA_LOCATION = "extra_location";


    private static final int
            MIN_TIME = 0,
            MIN_DISTANCE = 0;

    private long mLastLocationMillis;
    private Location lastLocation;
    private boolean isGPSFix = false;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("BOOM", "CREATE");
        setGps();
    }

    private void sendBroadcastMessage(Location location) {
        if (location != null) {
            Intent broadcast = new Intent();
            broadcast.setAction(MyService.ACTION_LOCATION_BROADCAST);
            broadcast.addCategory(Intent.CATEGORY_DEFAULT);

            broadcast.putExtra(EXTRA_LOCATION, location);

            sendBroadcast(broadcast);
            //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.d("BOOM", "SEND");
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        setGps();
    }

    public void setGps(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.addGpsStatusListener(this);
        sendBroadcastMessage(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        sendBroadcastMessage(location);
                        mLastLocationMillis = SystemClock.elapsedRealtime();
                        lastLocation = location;
                        Log.d("BOOM", "Changed");
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                }
        );
    }

    @Override
    public void onGpsStatusChanged(int event) {
        Intent broadcast = new Intent();
        broadcast.setAction(MyService.ACTION_LOCATION_BROADCAST);
        broadcast.addCategory(Intent.CATEGORY_DEFAULT);
        broadcast.putExtra(EXTRA_ISGPSFIX, isGPSFix);

        switch (event){
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                if(lastLocation!=null){
                    isGPSFix = (SystemClock.elapsedRealtime() - mLastLocationMillis) < 3000;
                }
                if (isGPSFix) { // A fix has been acquired.
                    // Do something.
                    Log.d("BOOM", "GPS FIX");
                    isGPSFix = true;
                } else { // The fix has been lost.
                    // Do something.
                    Log.d("BOOM", "GPS NOT FIX");
                    isGPSFix = false;
                }

                broadcast.putExtra(EXTRA_ISGPSFIX, isGPSFix);
                sendBroadcast(broadcast);


                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                // Do something.
                isGPSFix = true;


                broadcast.putExtra(EXTRA_ISGPSFIX, isGPSFix);
                sendBroadcast(broadcast);

                break;
        }
    }
}
