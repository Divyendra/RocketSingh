package org.rocketsingh.android.rocketsinghbiker.ui.Login;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.squareup.wire.Wire;

import org.rocketsingh.android.rocketsinghbiker.R;
import org.rocketsingh.android.rocketsinghbiker.realmmodels.BikerDetail;
import org.rocketsingh.android.rocketsinghbiker.ui.BikerMap.BikerMapActivity;
import org.rocketsingh.android.rocketsinghbiker.utilities.Constants;
import org.rocketsingh.android.rocketsinghbiker.utilities.SharedPrefHelper;
import org.rocketsingh.android.rocketsinghbiker.utilities.TaskFragment;

import java.io.IOException;

import io.realm.Realm;
import mobileresponse.LoginResponseBiker;
import biker.BikerProfile;

/**
 * Created by Divyu on 9/29/2015.
 */
public class LoginActivity extends AppCompatActivity implements TaskFragment.TaskCallbacks {
    EditText etPhno, etPwd;
    TextInputLayout tilPhno, tilPwd;
    Button loginBtn;
    Realm mRealm;
    boolean isLogging_in;

    private TaskFragment mTaskFragment;
    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etPhno = (EditText) findViewById(R.id.input_phno);
        etPwd = (EditText) findViewById(R.id.input_pwd);
        tilPhno = (TextInputLayout) findViewById(R.id.input_layout_phno);
        tilPwd = (TextInputLayout) findViewById(R.id.input_layout_pwd);
        loginBtn = (Button) findViewById(R.id.loginBtn);
        etPwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptlogin();
                    return true;
                }
                return false;
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (TaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
        // If the Fragment is not null, then it is currently being retained across a configuration change.
        if (mTaskFragment == null) {
            mTaskFragment = new TaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }

        mRealm = Realm.getInstance(this);
    }

    public void login(View v) {attemptlogin();}

    public void attemptlogin() {
        if (isLogging_in) return;

        //Reset errors
        tilPhno.setError(null);
        tilPwd.setError(null);

        View focusView = null;
        boolean cancel = false;

        String phNo = etPhno.getText().toString();
        String password = etPwd.getText().toString();

        if (TextUtils.isEmpty(phNo)) {
            tilPhno.setError(getString(R.string.error_field_empty));
            focusView = etPhno;
            cancel = true;
            Log.i(LOG_TAG, "phno empty");
        } else if (!isPhoneNoValid(phNo)) {
            tilPhno.setError(this.getString(R.string.error_phno_invalid));
            focusView = etPhno;
            cancel = true;
            Log.i(LOG_TAG, "phno invalid");
        }

        if (TextUtils.isEmpty(password)) {
            tilPwd.setError(getString(R.string.error_field_empty));
            focusView = etPwd;
            cancel = true;
            Log.i(LOG_TAG, "pwd empty");
        } else if (!isPasswordValid(password)) {
            tilPwd.setError(getString(R.string.error_password_short));
            focusView = etPwd;
            cancel = true;
            Log.i(LOG_TAG, "pwd invalid");
        }


        if (cancel) {
            focusView.requestFocus();
        } else {
            isLogging_in = true;
            loginBtn.setEnabled(false);
            loginBtn.setText("LoggingIn...");
            Object postArgs[] = new Object[2];
            postArgs[0] = getString(R.string.server_address) + getString(R.string.path_bikerlogin); //http://192.168.1.9:8809/
            BikerProfile bikerProfile = new BikerProfile.Builder()
                    .phone(phNo)
                    .password(password).build();
            postArgs[1] = bikerProfile.toByteArray();
            Log.i(LOG_TAG, "Sending details");
            mTaskFragment.startPostTaskProto_ResponseProto(postArgs);
        }


    }


    private boolean isPhoneNoValid(String phno) {
        return (phno.length() == 10)?true:false;
    }

    private boolean isPasswordValid(String pwd) {
        return (pwd.length() > 5)?true:false;
    }

    public void resetLoginProcess() {
        loginBtn.setEnabled(true);
        loginBtn.setText(getString(R.string.btn_text_login));
        isLogging_in = false;
    }

    @Override
    protected void onDestroy() {
        mRealm.close();
        super.onDestroy();
    }

    @Override
    public void onProgressUpdate(int percent){ }

    @Override
    public void onCancelled(final String reason) {
        Log.i(LOG_TAG, "Cancel Reason:" + reason);
        //onCancelled method is in this Activity but it is run from a background thread and not on that Activity's UI thread. So used this.runOnUiThread(new Runnable() { } ); to do UI operations.
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), reason, Toast.LENGTH_SHORT).show();
                resetLoginProcess();
            }
        });
    }

    @Override
    public void onPostExecute(Object result) {
        Log.i(LOG_TAG, "Results came back");
        Wire wire = new Wire();
        try {
            LoginResponseBiker  loginResponseBiker= wire.parseFrom((byte[])result, LoginResponseBiker.class);

            switch (loginResponseBiker.status) {
                case Constants.ResponseStatusCode.SUCCESS:
                    loginBtn.setText("DONE");
                    Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
                    //Save UserProfile here
                    BikerDetail bikerDetail = new BikerDetail();
                    bikerDetail.setPhoneNo(loginResponseBiker.biker_profile.phone);
                    bikerDetail.setAltPhno(loginResponseBiker.biker_profile.alternate_phone);
                    bikerDetail.setBikeLicensePlate(loginResponseBiker.biker_profile.bike_license_plate);
                    bikerDetail.setName(loginResponseBiker.biker_profile.first_name);
                    bikerDetail.setBikerType(loginResponseBiker.biker_profile.biker_type);
                    bikerDetail.setEmail(loginResponseBiker.biker_profile.email_id);
                    bikerDetail.setDateCreated(loginResponseBiker.biker_profile.date_created);
                    mRealm.beginTransaction();
                    mRealm.copyToRealmOrUpdate(bikerDetail);
                    mRealm.commitTransaction();
                    SharedPrefHelper.setKey_isLoggedIn(this, true);
                    //GO TO HOMESCREEN ACTIVITY
                    Intent intent = new Intent(this, BikerMapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    break;
                case Constants.ResponseStatusCode.FAILED:
                    Toast.makeText(getApplicationContext(), "Credentials incorrect. Please try again", Toast.LENGTH_SHORT).show();
                    resetLoginProcess();
                    break;
            }
        }catch (IOException e) {
            //Send log to dev on this crash
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "While logging in");
            Crashlytics.logException(e);
            e.printStackTrace();
            resetLoginProcess();
        }
        catch (Exception e) {
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "While logging in");
            Crashlytics.logException(e);
            e.printStackTrace();
            resetLoginProcess();
        }
    }
}
