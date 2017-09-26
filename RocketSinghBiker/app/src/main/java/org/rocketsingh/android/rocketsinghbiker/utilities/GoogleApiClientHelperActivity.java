package org.rocketsingh.android.rocketsinghbiker.utilities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

/**
 * Created by Divyu on 7/7/2015.
 */

//The below class implemented keeping this docs in mind : https://developers.google.com/android/guides/api-client#Starting
//This class is useful for abstracting
public class GoogleApiClientHelperActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected GoogleApiClient mGoogleApiClient;
    protected boolean mFlagResolvingError = false;
    protected boolean mFlagGooglePlayConnectionPresent = false;
    private boolean mSetupNotNeeded = false;

    // Request code to use when launching the resolution activity and refering it back onActivityResult()
    private static final int CODE_REQUEST_RESOLVE_ERROR = 1001;
    private static final String ARG_DIALOG_ERROR = "dialogerrorcode";

    /**
    **Created to have option of enabling/disabling setup of googlapiclient.
    ** @param  setupNeeded - If GoogleApiClient usage is needed or not
    */
    protected void isGoogleApiClientSetupNeeded (boolean setupNeeded) {
        mSetupNotNeeded = !setupNeeded;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!mSetupNotNeeded) {
            // Create a GoogleApiClient instance
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Places.GEO_DATA_API)            //Needed for getting AutoComplete Suggestions from Google.
                            //.addApi(Places.PLACE_DETECTION_API)
                    .addApi(LocationServices.API)           //Needed for getting latest location using FusedLocationApi
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mSetupNotNeeded) {
            if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!mSetupNotNeeded) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Connected to Google Play services!
        mFlagGooglePlayConnectionPresent = true;
        onConnectionCalled();
    }

    public void onConnectionCalled() {
        //Used for Child classes as callback.The good stuff goes here.
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
        mFlagGooglePlayConnectionPresent = false;
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mFlagResolvingError) {
            return;  //Returning as already an error is being resolved. USeful to handle repetitive Conn fails during config chnages
        } else if (connectionResult.hasResolution()) {
            try {
                mFlagResolvingError = true;
                connectionResult.startResolutionForResult(this, CODE_REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();  //Try again
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
            mFlagResolvingError = true;  //Donot want to resolve again while showing error dialog.
        }
    } //End of onConnectionFailed

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);  //Useful for calling Fragment's onActivityResult()

        if (requestCode == CODE_REQUEST_RESOLVE_ERROR) {   //Returned back from the call 'startResolutionForResult(...)'
            mFlagResolvingError = false;
            if (resultCode == RESULT_OK) {
                if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    public void showErrorDialog(int errorCode) {
        GoogleApiClientErrorDialogFragment.newInstance(errorCode).show(getSupportFragmentManager(), "errordialogfragmenttag");
    }

    /* Called from GoogleApiClientErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mFlagResolvingError = false;
    }

    //Below Fragment is for display errordialog.
    public static class GoogleApiClientErrorDialogFragment extends DialogFragment {

        static GoogleApiClientErrorDialogFragment newInstance(int errorCode) {
            GoogleApiClientErrorDialogFragment googleApiClientErrorDialogFragment = new GoogleApiClientErrorDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(ARG_DIALOG_ERROR, errorCode);
            googleApiClientErrorDialogFragment.setArguments(bundle);
            return googleApiClientErrorDialogFragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int errorCode = this.getArguments().getInt(ARG_DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), CODE_REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            ((GoogleApiClientHelperActivity)getActivity()).onDialogDismissed();
        }
    }
}
