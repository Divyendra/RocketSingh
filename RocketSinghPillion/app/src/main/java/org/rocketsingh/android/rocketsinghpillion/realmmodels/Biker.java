package org.rocketsingh.android.rocketsinghpillion.realmmodels;

import io.realm.RealmObject;

/**
 * Created by Divyu on 10/10/2015.
 */
public class Biker extends RealmObject {
    private String phNo;
    private String name;
    private String rating;
    private String licensePlate;
    //The below is used for sending CustomerLatLng to Biker in a pubsubway while Trip is in progress and biker yet to reach customer stage.
    private String gcmToken;

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getPhNo() {
        return phNo;
    }

    public void setPhNo(String phNo) {
        this.phNo = phNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getGcmToken() {
        return gcmToken;
    }

    public void setGcmToken(String gcmToken) {
        this.gcmToken = gcmToken;
    }
}
