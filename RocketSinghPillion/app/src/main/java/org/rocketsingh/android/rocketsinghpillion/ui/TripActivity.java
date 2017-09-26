package org.rocketsingh.android.rocketsinghpillion.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.wire.Wire;

import org.rocketsingh.android.rocketsinghpillion.R;
import org.rocketsingh.android.rocketsinghpillion.realmmodels.Customer;
import org.rocketsingh.android.rocketsinghpillion.realmmodels.Trip;
import org.rocketsingh.android.rocketsinghpillion.ui.BikerMap.BikerMapActivity;
import org.rocketsingh.android.rocketsinghpillion.ui.navdrawer.RSDrawerActivitywithGoogleApi;
import org.rocketsingh.android.rocketsinghpillion.utilities.CommonUtilities;
import org.rocketsingh.android.rocketsinghpillion.utilities.Constants;
import org.rocketsingh.android.rocketsinghpillion.utilities.SharedPrefHelper;
import org.rocketsingh.android.rocketsinghpillion.utilities.TaskFragment;

import java.io.IOException;

import in.raveesh.pacemaker.Pacemaker;
import io.realm.Realm;
import io.realm.RealmChangeListener;

import biker.BikerLocation;
import mobilerequest.RequestData;
/**
 * Created by Divyu on 10/10/2015.
 */
public class TripActivity extends RSDrawerActivitywithGoogleApi implements TaskFragment.TaskCallbacks, GoogleMap.OnCameraChangeListener {
    TextView tvBikerName, tvPhNo, tvVehicleNo;
    ImageView mCallImage;
    Button cancelBtn;
    Realm mRealm;
    RealmChangeListener mRealmChangeListener;
    private static final String LOG_TAG = TripActivity.class.getSimpleName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private static final float DEFAULT_MAP_ZOOM_LEVEL = 15;
    private static float CURRENT_MAP_ZOOM_LEVEL = DEFAULT_MAP_ZOOM_LEVEL;
    private LatLng mLastLatLng;
    private Trip mTrip;
    private long mTripTimeID;
    private Customer mCustomer;
    Animation mFlasherAnim;
    FrameLayout mMainFrame;
    View mProgressBar;

    private TaskFragment mTaskFragment;
    private static final String TAG_TASK_FRAGMENT = "task_fragment";

    private static final int TYPE_CANCELREQUEST = 1, TYPE_GETBIKERLOCATION = 2;

    class MyRealmChangeListener implements RealmChangeListener {
        @Override
        public void onChange() {
            if (SharedPrefHelper.Realm_Trips.getInsertType(TripActivity.this) == SharedPrefHelper.Realm_Trips.TRIPUPDATE_STARTED) {
                //Remove biker marker and remove cancel button.
                mMap.clear();
                cancelBtn.setVisibility(View.GONE);
            }
            if (SharedPrefHelper.Realm_Trips.getInsertType(TripActivity.this) == SharedPrefHelper.Realm_Trips.TRIPUPDATE_FINISHED) {
                //TODO:: Show The tripdetail aka payment dialog.
                showPaymentDialog();
            }
            SharedPrefHelper.Realm_Trips.putInsertType(TripActivity.this, -1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        isGoogleApiClientSetupNeeded(true);
        super.onCreate(savedInstanceState);
        createMenuDrawer(R.layout.activity_trip);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); //To make trip title invisible
        }
        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (TaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
        if (mTaskFragment == null) {
            mTaskFragment = new TaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }
        setUpMapIfNeeded();
        tvBikerName = (TextView) findViewById(R.id.bikerName);
        tvPhNo = (TextView) findViewById(R.id.bikerPhno);
        mCallImage = (ImageView) findViewById(R.id.callImage);
        tvVehicleNo = (TextView) findViewById(R.id.bikerVehicleNo);
        cancelBtn = (Button) findViewById(R.id.cancelRequestBtn);
        mMainFrame = (FrameLayout) findViewById(R.id.mainContainer);

        mRealm = Realm.getInstance(this);
        mTrip = mRealm.where(Trip.class).equalTo("status", Constants.TripStatusCode.TRIP_ACTIVE)
                .or().equalTo("status", Constants.TripStatusCode.TRIP_STARTED).findFirst();
        if (mTrip == null) {
            Crashlytics.logException(new NullPointerException("In TripActivity: ACTIVE or" +
                    "STARTED Trips are ZERO"));
            SharedPrefHelper.setKey_isOnTrip(this, false);
            finish();
        }
        mTripTimeID = mTrip.getTimeId();
        Log.i(LOG_TAG, "TimeID in concern: " + mTripTimeID);
        tvBikerName.setText(mTrip.getBikerAlloted().getName());
        tvPhNo.setText(mTrip.getBikerAlloted().getPhNo());
        tvVehicleNo.setText(mTrip.getBikerAlloted().getLicensePlate());
        Log.i(LOG_TAG, "Trip Status : " + mTrip.getStatus());
        if (mTrip.getStatus().equals(Constants.TripStatusCode.TRIP_ACTIVE)) {
            cancelBtn.setVisibility(View.VISIBLE);
        }
        mCustomer = mRealm.where(Customer.class).findFirst();
        mRealmChangeListener = new MyRealmChangeListener();
        mRealm.addChangeListener(mRealmChangeListener);
        getBikerAllotedLocation();

        mFlasherAnim = AnimationUtils.loadAnimation(this, R.anim.viewflasher);
        mCallImage.startAnimation(mFlasherAnim);

    }

    private void getBikerAllotedLocation() {
        Object[] postArgs = new Object[2];
        postArgs[0] = getString(R.string.server_address) + getString(R.string.path_getBikerLocation)
                + "/" + mTrip.getBikerAlloted().getPhNo();
        mTaskFragment.startGetTaskProto(postArgs, TYPE_GETBIKERLOCATION);
    }

    public void cancelTrip(View v) {
        showProgress();
        Object[] postArgs = new Object[2];
        postArgs[0] = getString(R.string.server_address) + getString(R.string.path_cancelRequest);
        postArgs[1] = new RequestData.Builder()
                .entity_id(mCustomer.getPhoneNo())
                .entity_type(Constants.Entities.CUSTOMER)
                .time_id(String.valueOf(mTrip.getTimeId())).build().toByteArray();
        mTaskFragment.startPostTaskProto_ResponseStatus(postArgs, TYPE_CANCELREQUEST);
    }

    public void callBiker(View v) {
        Intent callBikerIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mTrip.getBikerAlloted().getPhNo()));
        startActivity(callBikerIntent);
    }

    private void onTripCancelSuccess() {
        mRealm.beginTransaction();
        mTrip.setStatus(Constants.TripStatusCode.TRIP_CANCELLED);
        mRealm.commitTransaction();
        SharedPrefHelper.setKey_isOnTrip(this, false);
        Intent intent = new Intent(this, BikerMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void showProgress() {
        Log.i(LOG_TAG, "showProgress");
        mProgressBar= CommonUtilities.getProgressBar(this, "Cancelling Request");
        mMainFrame.addView(mProgressBar);
    }

    private void removeProgress() {
        Log.i(LOG_TAG, "removeProgress");
        mMainFrame.removeView(mProgressBar);
    }

    @Override
    public void onPostExecute(Object result, int REQUESTTYPE) {
        Wire wire = new Wire();
        try {
            switch (REQUESTTYPE) {
                case TYPE_GETBIKERLOCATION:
                    BikerLocation bikerLocation = wire.parseFrom((byte[]) result, BikerLocation.class);
                    mMap.clear();
                    LatLng latLng = new LatLng(bikerLocation.current_lat, bikerLocation.current_long);
                    mMap.addMarker(new MarkerOptions().position(latLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.biker_mapmarker)));
                    break;
                case TYPE_CANCELREQUEST:
                    removeProgress();
                    switch ((String)result) {
                        case Constants.ResponseStatusCode.SUCCESS:
                            Toast.makeText(getApplicationContext(), "Trip Cancelled", Toast.LENGTH_LONG).show();
                            onTripCancelSuccess();
                            break;
                        case Constants.ResponseStatusCode.CONDITIONAL_UPDATE_FAILED:
                            Toast.makeText(getApplicationContext(), "Trip cannot be cancellled once started",
                                    Toast.LENGTH_LONG).show();
                            break;
                        case Constants.ResponseStatusCode.FAILED:
                            Toast.makeText(getApplicationContext(), "Failed to cancel. Please try again",
                                    Toast.LENGTH_LONG).show();
                            break;
                    }
                    break;
            }
        }catch (IOException e) {
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "While Getting BikerLocation");
            Crashlytics.logException(e);
            Toast.makeText(this, "Getting BikerLocation failed", Toast.LENGTH_LONG);
            e.printStackTrace();
        } catch (Exception e) {
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "While Getting BikerLocation");
            Crashlytics.logException(e);
            Toast.makeText(this, "Getting BikerLocation failed", Toast.LENGTH_LONG);
            e.printStackTrace();
        }
        removeProgress();
    }

    private void showPaymentDialog() {
        Trip trip = mRealm.where(Trip.class).equalTo("timeId", mTripTimeID).findFirst();
        final AppCompatDialog dialog = new AppCompatDialog(TripActivity.this, R.style.DialogStyle);
        //Have to make dialog fullscreen
        dialog.setTitle("Trip Details");
        Log.i(LOG_TAG, "Triptimein dialog: " + trip.getTripTime() + " for id: " + mTripTimeID);
        dialog.setContentView(R.layout.dialog_tripdetails);
        TextView tvCost = (TextView)dialog.findViewById(R.id.tripCost);
        TextView tvDistance = (TextView)dialog.findViewById(R.id.tripDistance);
        TextView tvTime = (TextView)dialog.findViewById(R.id.tripTime);
        tvCost.setText(getString(R.string.format_amount_INR, trip.getTripCost()));
        tvDistance.setText(getString(R.string.format_distance, (trip.getTripDistance()/1000)));
        tvTime.setText(getString(R.string.format_time, trip.getTripTime()/60000));
        Button okBtn = (Button)dialog.findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Intent intent = new Intent(TripActivity.this, BikerMapActivity.class);
                startActivity(intent);
                finish();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Intent intent = new Intent(TripActivity.this, BikerMapActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onConnectionCalled() {
        if (mMap != null) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                mLastLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLatLng, CURRENT_MAP_ZOOM_LEVEL));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {// Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {// Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            mMap.setMyLocationEnabled(true);

            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
        }
    }


    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.i(LOG_TAG, "Zoom level is :" + cameraPosition.zoom);
        CURRENT_MAP_ZOOM_LEVEL = cameraPosition.zoom;
    }

    @Override
    public void onProgressUpdate(int percent) { }

    @Override
    public void onCancelled(String reason) {  this.runOnUiThread(new Runnable() {
        public void run() {
            Toast.makeText(getApplicationContext(), "Failed to Fetch BikerLocation", Toast.LENGTH_SHORT).show();
        }
    });  }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.removeAllChangeListeners();
        mRealm.close();
    }
}
