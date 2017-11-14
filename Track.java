package com.example.bartek.gpsservice;

import android.location.Location;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by bartek on 10.11.2017.
 */

public class Track {

    private String name;
    private ArrayList<Location> Aroute;
    private ArrayList<Float> Adistance;
    private ArrayList<Double> Atime;
    private ArrayList<Float> AtopSpeed;
    private ArrayList<Float> AavgSpeed;

    private float allDistance = 0f;
    private float allTime = 0f;
    private float topSpeed = 0f;
    private float avgSpeed = 0f;


    public Track(String name){
        this.name = name;
        Aroute = new ArrayList<Location>();
        Adistance = new ArrayList<Float>();
        Atime = new ArrayList<Double>();
        AtopSpeed = new ArrayList<Float>();
        AavgSpeed = new ArrayList<Float>();
    }

    public void addWayPoint(Location location, Float distance, Double time, Float topSpeed, Float avgSpeed ){
        Aroute.add(location);
        Adistance.add(distance);
        Atime.add(time);
        AtopSpeed.add(topSpeed);
        AavgSpeed.add(avgSpeed);

        if(topSpeed>this.topSpeed) this.topSpeed = topSpeed;

        Log.d("TRACK", "N A M E : "+name);
        Log.d("TRACK", Aroute.toString());
        Log.d("TRACK", "DISTANCE "+Adistance.toString());
        Log.d("TRACK", "TIME "+Atime.toString());
    }
    
    public void setEnd(){
        for (int i=0; i<Aroute.size(); i++){
            allDistance += Adistance.get(i);
            allTime += Atime.get(i);
            avgSpeed +=AavgSpeed.get(i);
        }
        avgSpeed /= AavgSpeed.size()-1;
        Log.d("TRACK", "ALL DIS: "+allDistance);
        Log.d("TRACK", "ALL TIME: "+allTime);
    }


    public float getDistance(){
        return Adistance.get(Adistance.size()-1);
    }

}
