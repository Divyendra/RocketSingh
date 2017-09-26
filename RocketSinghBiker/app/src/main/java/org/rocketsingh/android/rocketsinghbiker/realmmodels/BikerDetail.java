package org.rocketsingh.android.rocketsinghbiker.realmmodels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Divyu on 10/9/2015.
 */
public class BikerDetail extends RealmObject {
    @PrimaryKey
    private String phoneNo; //is the bikerid
    private String name;
    private String bikeLicensePlate;
    private String altPhno;
    private String bikerType;
    private long dateCreated;
    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBikeLicensePlate() {
        return bikeLicensePlate;
    }

    public void setBikeLicensePlate(String bikeLicensePlate) {
        this.bikeLicensePlate = bikeLicensePlate;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getAltPhno() {
        return altPhno;
    }

    public void setAltPhno(String altPhno) {
        this.altPhno = altPhno;
    }

    public String getBikerType() {
        return bikerType;
    }

    public void setBikerType(String bikerType) {
        this.bikerType = bikerType;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
