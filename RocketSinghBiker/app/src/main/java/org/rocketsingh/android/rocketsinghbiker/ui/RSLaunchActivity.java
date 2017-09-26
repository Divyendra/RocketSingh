package org.rocketsingh.android.rocketsinghbiker.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;

import org.rocketsingh.android.rocketsinghbiker.ui.BikerMap.BikerMapActivity;
import org.rocketsingh.android.rocketsinghbiker.ui.Login.RSUserAuthActivity;
import org.rocketsingh.android.rocketsinghbiker.utilities.SharedPrefHelper;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Divyu on 9/29/2015.
 */
public class RSLaunchActivity extends Activity {

    /*
    * this the launcher activity, which does nothing more if-loggedinCheck-module
    * Note that it's defined in the manifest to have no UI
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent;
        if (SharedPrefHelper.isLoggedIn(this)) {
            //TODO :: Replace second arg with target activity on logged-in
            intent = new Intent(this, BikerMapActivity.class);
        } else {
            intent = new Intent(this, RSUserAuthActivity.class);
        }
        if (!Fabric.isInitialized()) {
            Fabric.with(this, new Crashlytics());
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
