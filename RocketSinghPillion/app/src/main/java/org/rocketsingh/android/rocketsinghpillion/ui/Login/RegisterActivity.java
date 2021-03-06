package org.rocketsingh.android.rocketsinghpillion.ui.Login;

import customer.CustomerProfile;
import io.realm.Realm;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.rocketsingh.android.rocketsinghpillion.R;
import org.rocketsingh.android.rocketsinghpillion.realmmodels.Customer;
import org.rocketsingh.android.rocketsinghpillion.ui.BikerMap.BikerMapActivity;
import org.rocketsingh.android.rocketsinghpillion.utilities.Constants;
import org.rocketsingh.android.rocketsinghpillion.utilities.SharedPrefHelper;
import org.rocketsingh.android.rocketsinghpillion.utilities.TaskFragment;

/**
 * Created by Divyu on 9/29/2015.
 */
public class RegisterActivity extends AppCompatActivity implements TaskFragment.TaskCallbacks {
    EditText etPhno, etPwd, etRePwd, etName, etRefCode, etEmail;
    TextInputLayout tilPhno, tilPwd, tilRePwd, tilName, tilRefCode, tilEmail;
    boolean isRegistering;
    Button registerBtn;
    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private TaskFragment mTaskFragment;
    private static final String LOG_TAG = RegisterActivity.class.getSimpleName();

    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etPhno = (EditText) findViewById(R.id.input_phno);
        etPwd = (EditText) findViewById(R.id.input_pwd);
        etRePwd = (EditText) findViewById(R.id.input_reenterpwd);
        etName = (EditText) findViewById(R.id.input_name);
        etRefCode = (EditText) findViewById(R.id.input_referralcode);
        etEmail = (EditText) findViewById(R.id.input_email);
        registerBtn = (Button) findViewById(R.id.registerBtn);
        tilPhno = (TextInputLayout) findViewById(R.id.input_layout_phno);
        tilPwd = (TextInputLayout) findViewById(R.id.input_layout_pwd);
        tilRePwd = (TextInputLayout) findViewById(R.id.input_layout_reenterpwd);
        tilName = (TextInputLayout) findViewById(R.id.input_layout_name);
        tilRefCode = (TextInputLayout) findViewById(R.id.input_layout_referralcode);
        tilEmail = (TextInputLayout) findViewById(R.id.input_layout_email);


        etRefCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.register || id == EditorInfo.IME_NULL) {
                    attemptRegistration();
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

    public void register(View v) { attemptRegistration();}

    String phNo, email, name;
    public void attemptRegistration() {
        if (isRegistering) return;
        //Reset errors
        tilName.setError(null);
        tilPhno.setError(null);
        tilPwd.setError(null);
        tilRePwd.setError(null);
        tilEmail.setError(null);
        tilRefCode.setError(null);

        View focusView = null;
        boolean cancel = false;

        phNo = etPhno.getText().toString();
        String password = etPwd.getText().toString();
        String rePassword = etRePwd.getText().toString();
        email = etEmail.getText().toString();
        name = etName.getText().toString();
        String refCode = etRefCode.getText().toString();

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

        if (TextUtils.isEmpty(rePassword)) {
            tilRePwd.setError(getString(R.string.error_field_empty));
            focusView = etRePwd;
            cancel = true;
            Log.i(LOG_TAG, "repwd empty");
        } else if (!password.equals(rePassword)) {
            tilRePwd.setError(getString(R.string.error_password_mismatch));
            focusView = etRePwd;
            cancel = true;
            Log.i(LOG_TAG, "repwd mismatch");
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_field_empty));
            focusView = etEmail;
            cancel = true;
            Log.i(LOG_TAG, "email empty");
        } else if (!isEmailValid(email)) {
            tilEmail.setError(getString(R.string.error_email_invalid));
            focusView = etEmail;
            cancel = true;
            Log.i(LOG_TAG, "email invalid");
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            isRegistering = true;
            registerBtn.setEnabled(false);
            registerBtn.setText("Registering...");
            Object postArgs[] = new Object[2];
            postArgs[0] = getString(R.string.server_address) + getString(R.string.path_pillionregister); //http://192.168.1.9:8809/
            CustomerProfile customerProfile = new CustomerProfile.Builder()
                    .phone_number(phNo)
                    .password(password)
                    .first_name(name)
                    .email_id(email)
                    .referral_code(refCode)
                    .device_token(SharedPrefHelper.getKey_Gcmtoken(this)).build();
            postArgs[1] = customerProfile.toByteArray();
            Log.i(LOG_TAG, "Sending detials");
            mTaskFragment.startPostTaskProto_ResponseStatus(postArgs, -1);
        }

    }


    private boolean isPhoneNoValid(String phno) {
        return (phno.length() == 10)?true:false;
    }

    private boolean isPasswordValid(String pwd) {
        return (pwd.length() > 5)?true:false;
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onProgressUpdate(int percent) {  }

    @Override
    public void onCancelled(final String reason) {
        Log.i(LOG_TAG, "Cancel Reason:" + reason);
        //onCancelled method is in this Activity but it is run from a background thread and not on that Activity's UI thread. So used this.runOnUiThread(new Runnable() { } ); to do UI operations.
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), reason, Toast.LENGTH_SHORT).show();
                resetRegistration();
            }
        });
    }

    public void resetRegistration() {
        registerBtn.setEnabled(true);
        registerBtn.setText(getString(R.string.btn_text_register));
        isRegistering = false;
    }

    @Override
    public void onPostExecute(Object result, int REQUEST_TYPE) {
        Log.i(LOG_TAG, "Results came back");
        try {
            switch ((String) result) {
                case Constants.ResponseStatusCode.SUCCESS:
                    registerBtn.setText("DONE");
                    Toast.makeText(getApplicationContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                    //Save UserProfile here
                    Customer customer = new Customer();
                    customer.setName(name);
                    customer.setPhoneNo(phNo);
                    customer.setEmailId(email);
                    mRealm.beginTransaction();
                    mRealm.copyToRealmOrUpdate(customer);
                    mRealm.commitTransaction();
                    SharedPrefHelper.setKey_isLoggedIn(this, true);
                    //GO TO HOMESCREEN ACTIVITY
                    Intent intent = new Intent(this, BikerMapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    break;
                case Constants.ResponseStatusCode.DUPLICATE:
                    Toast.makeText(getApplicationContext(), "UserName already exists. Please use the same account", Toast.LENGTH_SHORT).show();
                    resetRegistration();
                    break;
                case Constants.ResponseStatusCode.FAILED:
                    Toast.makeText(getApplicationContext(), "Registartion has failed. Log has been sent to check Issue", Toast.LENGTH_SHORT).show();
                    resetRegistration();
                    break;
            }
        } catch (Exception e) {
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "While Registering");
            Crashlytics.logException(e);
            Toast.makeText(getApplicationContext(), "Registartion has failed. Log has been sent to check Issue", Toast.LENGTH_SHORT).show();
            resetRegistration();
        }
    }
}
