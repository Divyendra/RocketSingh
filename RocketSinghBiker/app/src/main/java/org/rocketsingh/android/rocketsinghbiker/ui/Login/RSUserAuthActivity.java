package org.rocketsingh.android.rocketsinghbiker.ui.Login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.rocketsingh.android.rocketsinghbiker.GCM.GCMRegistrationIntentService;
import org.rocketsingh.android.rocketsinghbiker.R;
import org.rocketsingh.android.rocketsinghbiker.utilities.GoogleApiClientHelperActivity;

/**
 * Created by Divyu on 9/29/2015.
 */
public class RSUserAuthActivity extends GoogleApiClientHelperActivity {
    BroadcastReceiver mGCMRegReceiver;
    private static final String LOG_TAG = RSUserAuthActivity.class.getSimpleName();
    Button signInBtn, registerBtn;
    boolean googleApiConnectionFlag, gcmRegFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rsuserauth);
        signInBtn = (Button) findViewById(R.id.btnSignIn);
        registerBtn = (Button) findViewById(R.id.btnRegister);

        mGCMRegReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean tokenSent = sharedPreferences.getBoolean(GCMRegistrationIntentService.PREF_KEY_GCMREGISTRATION, false);
                if (tokenSent) {
                    gcmRegFlag = true;
                    Log.i(LOG_TAG, "GCMREG: true");
                    if (googleApiConnectionFlag) enableBtns();
                } else {
                    Toast.makeText(RSUserAuthActivity.this, "Apologies. Unable to setup the app. Please restart the app with Internet access", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        };

        // check the device for a compatible Google Play services APK before accessing Google Play services features
        if(checkPlayServices()) {
            Intent regServiceIntent = new Intent(this, GCMRegistrationIntentService.class);
            startService(regServiceIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mGCMRegReceiver, new IntentFilter(GCMRegistrationIntentService.INTENTACTION_REGISTRATIONCOMPLETE));
    }
    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGCMRegReceiver);
    }

    public void enableBtns() {
        Log.i(LOG_TAG, "Buttons enabled");
        signInBtn.setVisibility(View.VISIBLE);
        registerBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnectionCalled() {
        Log.i(LOG_TAG, "GoogleAPI: true");
        googleApiConnectionFlag = true;
        if (gcmRegFlag) enableBtns();
    }

    //Call Register Activity here
    public void register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    //Call SignIn Activity here
    public void signIn(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.Issue being faced with its Google Play service version");
                Toast.makeText(this, "Apologies. The device is not supported.Issue being faced with its Google Play service version", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

}
