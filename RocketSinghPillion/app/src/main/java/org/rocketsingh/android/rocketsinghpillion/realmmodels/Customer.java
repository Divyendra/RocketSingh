package org.rocketsingh.android.rocketsinghpillion.realmmodels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Divyu on 10/10/2015.
 */
public class Customer extends RealmObject {
    @PrimaryKey
    private String phoneNo;
    private String emailId;
    private String name;
    private String altPhno;
    private String emergencyPhno;

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAltPhno() {
        return altPhno;
    }

    public void setAltPhno(String altPhno) {
        this.altPhno = altPhno;
    }

    public String getEmergencyPhno() {
        return emergencyPhno;
    }

    public void setEmergencyPhno(String emergencyPhno) {
        this.emergencyPhno = emergencyPhno;
    }
}
