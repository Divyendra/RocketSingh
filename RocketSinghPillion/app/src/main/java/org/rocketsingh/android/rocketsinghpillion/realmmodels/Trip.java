package org.rocketsingh.android.rocketsinghpillion.realmmodels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Divyu on 10/5/2015.
 */
public class Trip extends RealmObject {
    @PrimaryKey
    private long timeId;
    private String status;
    private String origin;
    private double tripDistance;
    private long tripTime;
    private double tripCost;
    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;
    private Biker bikerAlloted;

    public double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(double endLongitude) {
        this.endLongitude = endLongitude;
    }

    public double getTripCost() {
        return tripCost;
    }

    public void setTripCost(double tripCost) {
        this.tripCost = tripCost;
    }

    public long getTripTime() {
        return tripTime;
    }

    public void setTripTime(long tripTime) {
        this.tripTime = tripTime;
    }

    public double getTripDistance() {
        return tripDistance;
    }

    public void setTripDistance(double tripDistance) {
        this.tripDistance = tripDistance;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimeId() {
        return timeId;
    }

    public void setTimeId(long timeId) {
        this.timeId = timeId;
    }

    public Biker getBikerAlloted() {
        return bikerAlloted;
    }

    public void setBikerAlloted(Biker bikerAlloted) {
        this.bikerAlloted = bikerAlloted;
    }
}
