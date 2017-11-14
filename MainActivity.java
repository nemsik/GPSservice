package com.example.bartek.gpsservice;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private TextView textSpeed, textTopSpeed, textAvgSpeed, textTime, textStatus, textDistance;
    private Button start, wayPoint;


    private Location location, lastLocation, startLocation;

    private float speed;
    private float avgspped = 0.0f;
    private float currentAvgspped = 0.0f;
    private int countavg = 0;
    private int currentCountavg = 0;
    private float alldistande = 0;
    private double time;
    private double distance = 0d;
    private float topSpeed = 0.0f;
    private float currentTopSpeed = 0.0f;
    private float currentDistance = 0f;

    private boolean isGPSFix = false;
    private boolean GPSstatus = false;

    private boolean isStarted = false;

    private Date today;
    private String reportDate;
    private DateFormat df;

    private ArrayList<Track> ATrack;
    private int countAtrack = 0;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                location = intent.getParcelableExtra(MyService.EXTRA_LOCATION);

                double startTime = location.getTime();
                try {
                    double endTime = lastLocation.getTime();
                    time = (startTime-endTime);
                }catch (Exception e){
                    e.printStackTrace();
                }

                if(lastLocation != null && location!=lastLocation) {
                    countavg++;
                    currentCountavg++;
                    distance = location.distanceTo(lastLocation);

                    currentDistance += distance;


                    //speed = (float) (distance/time)*3600;
                    speed = (location.getSpeed()*3600)/1000;

                    textSpeed.setText(String.format("%.1f",speed) + " km/h");

                    if(speed<300){
                        if(speed>topSpeed){
                            topSpeed = speed;
                            textTopSpeed.setText(String.format("%.1f",topSpeed) +" km/h");
                        }
                        if(speed>currentTopSpeed) currentTopSpeed = speed;
                        avgspped += speed;
                        currentAvgspped += speed;
                        textAvgSpeed.setText(String.format("%.1f", avgspped/countavg ) +" km/h");
                        Log.d("BOOM", "AVG: "+avgspped/countavg);
                    }

                    alldistande+=distance;
                    if(alldistande <= 1000){
                        textDistance.setText(String.format("%.1f", alldistande) + " m");
                    }else
                    {
                        textDistance.setText(String.format("%.1f", alldistande/1000) + " km");
                    }

                }


                lastLocation = location;
                Log.d("BOOM", "RECEIVED LOC");
            }catch (Exception e){
                Log.d("BOOM", "ERR RECIEVED");
            }
            try {
                isGPSFix = intent.getBooleanExtra(MyService.EXTRA_ISGPSFIX, false);
                if(isGPSFix){
                    textStatus.setText("GPS OK!");
                    textStatus.setTextColor(Color.GREEN);
                }
                else {
                    textStatus.setText("GPS NOT FIX");
                    textStatus.setTextColor(Color.RED);
                }
            }catch (Exception e){
                Log.d("BOOM", "ERR RECIEVED LOC");
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.reset:
                reset();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textSpeed = (TextView) findViewById(R.id.textSpeed);
        textTopSpeed = (TextView) findViewById(R.id.textTopSpeed);
        textAvgSpeed = (TextView) findViewById(R.id.textAvgSpeed);
        textTime = (TextView) findViewById(R.id.textTime);
        textStatus = (TextView) findViewById(R.id.textStatus);
        textDistance = (TextView) findViewById(R.id.textDistance);

        start = (Button) findViewById(R.id.start);
        wayPoint = (Button) findViewById(R.id.wayPoint);

        isStarted = false;


        ATrack = new ArrayList<Track>();
        final TrackAdapter trackAdapter = new TrackAdapter(this, ATrack);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(trackAdapter);



        IntentFilter filter = new IntentFilter();
        filter.addAction(MyService.ACTION_LOCATION_BROADCAST);
        filter.addCategory(Intent.CATEGORY_DEFAULT);


        boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        registerReceiver(br, filter);

        if (permissionGranted) startService(new Intent(MainActivity.this, MyService.class));
        else ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);


        df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");



        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isStarted){
                    if(location!=null){
                        currentDistance = 0;
                        startLocation = location;
                        currentAvgspped = 0;
                        currentCountavg = 0;
                        currentTopSpeed = 0;

                        today = Calendar.getInstance().getTime();
                        reportDate = df.format(today);
                        ATrack.add(new Track(reportDate));
                        countAtrack = ATrack.size();
                        ATrack.get(countAtrack-1).addWayPoint(startLocation, 0.0f, 0.0d, 0f, 0f);
                        isStarted = true;
                        start.setText("STOP");
                        wayPoint.setEnabled(true);
                        trackAdapter.add(ATrack.get(countAtrack-1));

                    }
                }else{
                    currentAvgspped /= currentCountavg;
                   // ATrack.get(countAtrack-1).setFinished();
                    ATrack.get(countAtrack-1).addWayPoint(location, currentDistance, (double) location.getTime()-startLocation.getTime(), currentTopSpeed, currentAvgspped);
                    ATrack.get(countAtrack-1).setEnd();
                    wayPoint.setEnabled(false);
                    start.setText("START");
                    isStarted = false;
                    currentDistance = 0;
                    trackAdapter.add(ATrack.get(countAtrack-1));
                }
            }
        });

        wayPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentAvgspped /= currentCountavg;
                ATrack.get(countAtrack-1).addWayPoint(location, currentDistance, (double) location.getTime()-startLocation.getTime(), currentTopSpeed, currentAvgspped);
                startLocation = location;
                currentDistance = 0;
                currentAvgspped = 0;
                currentCountavg = 0;
                currentTopSpeed = 0;
                trackAdapter.notifyDataSetChanged();
                trackAdapter.add(ATrack.get(countAtrack-1));


            }
        });

    }

    public class TrackAdapter extends ArrayAdapter<Track>{
        public TrackAdapter(Context context, ArrayList<Track> tracks){
            super(context, R.layout.item_track ,tracks);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Track track = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_track, parent, false);

            }

            TextView trackName = (TextView) convertView.findViewById(R.id.trackName);
            TextView trackDistance = (TextView) convertView.findViewById(R.id.trackDistance);

            trackDistance.setText(track.getDistance()+ " m");


            return convertView;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(MainActivity.this, MyService.class));

        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        try {
            topSpeed = sharedPreferences.getFloat("TOP", 0.0f);
            avgspped = sharedPreferences.getFloat("AVG", 0.0f);

            alldistande = sharedPreferences.getFloat("DISTANCE", 0.0f);
            countavg = sharedPreferences.getInt("COUNTAVG", 0);

            textTopSpeed.setText(String.format("%.1f", topSpeed) + " km/h");
            textAvgSpeed.setText(String.format("%.1f", avgspped/countavg) + " km/h");

            if(alldistande <= 1000){
                textDistance.setText(String.format("%.1f", alldistande) + " m");
            }else
            {
                textDistance.setText(String.format("%.1f", alldistande/1000) + " km");
            }

        }catch (Exception e){
            Log.d("e", e.getMessage());
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(new Intent(MainActivity.this, MyService.class));
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putFloat("TOP", topSpeed);
        editor.putFloat("AVG", avgspped);
        editor.putFloat("DISTANCE", alldistande);
        editor.putInt("COUNTAVG", countavg);
        editor.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(MainActivity.this, MyService.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 200: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService(new Intent(MainActivity.this, MyService.class));
                }
            }
        }
    }

    private void reset() {
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putFloat("TOP", 0f);
        editor.putFloat("AVG", 0f);
        editor.putFloat("DISTANCE", 0f);
        editor.putInt("COUNTAVG", 0);
        editor.putLong("STARTTIME", SystemClock.uptimeMillis());
        editor.commit();

        topSpeed = 0.0f;
        avgspped = 0.0f;
        alldistande = 0.0f;
        countavg = 0;

        textTopSpeed.setText(String.format("%.1f", 0.0f) + " km/h");
        textAvgSpeed.setText(String.format("%.1f", 0.0f) + " km/h");
        textDistance.setText(String.format("%.1f", 0.0f) + " m");

    }
}
