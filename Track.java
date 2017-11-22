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
    private int seconds, minutes;


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
    
    public String getEnd(){
        allDistance = 0f;
        allTime = 0f;
        avgSpeed = 0f;
        for (int i=0; i<Aroute.size(); i++){
            allDistance += Adistance.get(i);
            allTime += Atime.get(i);
            avgSpeed +=AavgSpeed.get(i);
        }
        avgSpeed /= (AavgSpeed.size()-1);

        seconds = (int) (allTime/1000);
        minutes = seconds / 60;
        seconds %= 60;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n");
        stringBuilder.append("Total distance: ");
        stringBuilder.append(allDistance);
        stringBuilder.append("\nTime: ");
        stringBuilder.append(minutes + " m ");
        stringBuilder.append(seconds + " s");
        stringBuilder.append("\nAVG : ");
        stringBuilder.append(avgSpeed);

        return stringBuilder.toString();
    }

    public String getName(){
        return name;
    }

    public String getWaypoint(){
        String wayPoint = "";
        StringBuilder stringBuilder = new StringBuilder(wayPoint);

        for(int i=0 ; i<Aroute.size(); i++){
            stringBuilder.append("\nWaypoint: " + i);

            stringBuilder.append("\nDistance: ");
            if(Adistance.get(i) <= 1000) stringBuilder.append(Adistance.get(i) + " m");
            else stringBuilder.append(String.format("%.1f", Adistance.get(i)/1000) + " km");

            stringBuilder.append("\nTime: ");

            seconds = (int) (Atime.get(i)/1000);
            minutes = seconds / 60;
            seconds %= 60;
            stringBuilder.append(minutes+" min ");
            stringBuilder.append(seconds+" sec");


            stringBuilder.append("\nAVG: " + AavgSpeed.get(i) + " TOP: "+AtopSpeed.get(i));
            stringBuilder.append("\n");
        }

        wayPoint = stringBuilder.toString();
        return wayPoint;
    }
}
