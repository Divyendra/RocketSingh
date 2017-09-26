package org.rocketsingh.android.rocketsinghpillion.ui.BikerMap;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
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

import in.raveesh.pacemaker.Pacemaker;
import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.exceptions.RealmMigrationNeededException;
import mobilerequest.LatLngProto;
import biker.BikerLocation;
import mobilerequest.NewRequest;
import mobileresponse.MakeRequestResponse;
import mobileresponse.BikersNearByResponse;
import biker.BikersNearBy;

import org.rocketsingh.android.rocketsinghpillion.R;
import org.rocketsingh.android.rocketsinghpillion.realmmodels.Biker;
import org.rocketsingh.android.rocketsinghpillion.realmmodels.Customer;
import org.rocketsingh.android.rocketsinghpillion.realmmodels.Trip;
import org.rocketsingh.android.rocketsinghpillion.ui.TripActivity;
import org.rocketsingh.android.rocketsinghpillion.ui.navdrawer.RSDrawerActivitywithGoogleApi;
import org.rocketsingh.android.rocketsinghpillion.utilities.CommonUtilities;
import org.rocketsingh.android.rocketsinghpillion.utilities.Constants;
import org.rocketsingh.android.rocketsinghpillion.utilities.SharedPrefHelper;
import org.rocketsingh.android.rocketsinghpillion.utilities.TaskFragment;

import java.io.IOException;
import java.util.List;

/********NOTE********
 * CameraChangeListener of Maps is not useful for detecting drags on UI, so using customhack'TouchableWrapper' here.
 */
public class BikerMapActivity extends RSDrawerActivitywithGoogleApi implements TouchableWrapper.UpdateMapAfterUserInterection, TaskFragment.TaskCallbacks, GoogleMap.OnCameraChangeListener {
    private static final String LOG_TAG = BikerMapActivity.class.getSimpleName();

    private static final int TYPE_GETBIKERSNEARBY = 1, TYPE_MAKEAREQUEST = 2;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private static final float DEFAULT_MAP_ZOOM_LEVEL = 15;
    private static float CURRENT_MAP_ZOOM_LEVEL = DEFAULT_MAP_ZOOM_LEVEL;
    private LatLng mLastLatLng;

    private TaskFragment mTaskFragment;
    private static final String TAG_TASK_FRAGMENT = "task_fragment";

    private BikersNearByResponse mBikersNearBy;

    Button makeTripBtn;
    View mProgressBar;
    FrameLayout mMainFrame;

    Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPrefHelper.isOnTrip(this)) {
            //Mostly this condition wont be true.
            //TODO: Goto TripActivity.
            Intent intent = new Intent(this, TripActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        createMenuDrawer(R.layout.activity_biker_map);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
        }
        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (TaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
        if (mTaskFragment == null) {
            mTaskFragment = new TaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }
        setUpMapIfNeeded();
        makeTripBtn = (Button) findViewById(R.id.requestBikerBtn);
        mMainFrame = (FrameLayout) findViewById(R.id.mainContainer);

        try {
            mRealm = Realm.getInstance(this);
        } catch (RealmMigrationNeededException e) {
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.logException(e);
            Log.i(LOG_TAG, "Deleting Realm files instead of migrating");
            Realm.deleteRealmFile(BikerMapActivity.this);
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

            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
        }
    }

    @Override
    public void onConnectionCalled() {
        if (mMap != null) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                mLastLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                getBikersNearBy();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLatLng, CURRENT_MAP_ZOOM_LEVEL));
            }
        }
    }

    public void requestBiker(View v) {
        if (mBikersNearBy != null && mBikersNearBy.bikerLocations != null
                && mBikersNearBy.bikerLocations.size() == 0) return;

        Customer customer = mRealm.where(Customer.class).findFirst();
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLastLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }
        BikersNearBy bikersNearBy = new BikersNearBy.Builder()
                .bikerLocations(mBikersNearBy.bikerLocations).build();
        NewRequest newRequest = new NewRequest.Builder()
                .app_type(Constants.Entities.CUSTOMER)
                .bikerNearBy(bikersNearBy)
                .cur_latitude(mLastLatLng.latitude)
                .cur_longitude(mLastLatLng.longitude)
                .name(customer.getName())
                .cust_device_token(SharedPrefHelper.getKey_Gcmtoken(this))
                .phoneNo(customer.getPhoneNo()).build();
        Object[] postArgs = new Object[2];
        postArgs[0] = getString(R.string.server_address) + getString(R.string.path_makeARequest);
        postArgs[1] = newRequest.toByteArray();
        mTaskFragment.startPostTaskProto_ResponseProto(postArgs, TYPE_MAKEAREQUEST);
        startRequestSetup();
    }

    private  void getBikersNearBy() {
        Object postArgs[] = new Object[2];
        postArgs[0] = getString(R.string.server_address) + getString(R.string.path_getBikersNearBy); //http://192.168.1.9:8809/
        LatLngProto latLngProto = new LatLngProto.Builder()
                .latitude(mLastLatLng.latitude).longitude(mLastLatLng.longitude).build();
        postArgs[1] = latLngProto.toByteArray();
        mTaskFragment.startPostTaskProto_ResponseProto(postArgs, TYPE_GETBIKERSNEARBY);
    }

    private void noBikersFoundCallback() {
        mMap.clear();

        Toast.makeText(this, "No Bikers found Nearby", Toast.LENGTH_LONG).show();
    }

    //The Map has been moved and hence the marker on the map.
    @Override
    public void onUpdateMapAfterUserInterection() {
        mLastLatLng = mMap.getCameraPosition().target;
        Log.i(LOG_TAG, "Camera change detected" + mLastLatLng.latitude + ", " + mLastLatLng.longitude);
        //TODO:: Start Async Task here for fetching bikers
        getBikersNearBy();
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLatLng, CURRENT_MAP_ZOOM_LEVEL));
    }

    @Override
    public void onProgressUpdate(int percent) { }

    @Override
    public void onCancelled(String reason) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                requestFailedSetup(R.string.error_findbikers);
            }
        });
    }

    @Override
    public void onPostExecute(Object result,  int REQUESTTYPE) {
        Wire wire = new Wire();
        try {
            switch (REQUESTTYPE) {
                case TYPE_GETBIKERSNEARBY:
                    mBikersNearBy = wire.parseFrom((byte[]) result, BikersNearByResponse.class);
                    switch (mBikersNearBy.status) {
                        case Constants.ResponseStatusCode.SUCCESS:
                            refreshBikerMarkers(mBikersNearBy.bikerLocations);
                            break;
                        case Constants.ResponseStatusCode.NO_BIKERS_AVAILABLE:
                            noBikersFoundCallback();
                            break;
                        case Constants.ResponseStatusCode.FAILED:
                            Toast.makeText(getApplicationContext(), "Failed to Fetch Bikers", Toast.LENGTH_LONG).show();
                            break;
                    }
                    break;
                case TYPE_MAKEAREQUEST:
                    MakeRequestResponse requestAlloted = wire.parseFrom((byte[])result, MakeRequestResponse.class);
                    switch (requestAlloted.status) {
                        case Constants.ResponseStatusCode.SUCCESS:
                            Trip trip = new Trip();
                            trip.setTimeId(Long.valueOf(requestAlloted.time_id));
                            trip.setStatus(Constants.TripStatusCode.TRIP_ACTIVE);
                            Log.i(LOG_TAG, "timeID in makearequest : " + trip.getTimeId());
                            Biker biker = new Biker();
                            biker.setName(requestAlloted.bikerLocation.first_name);
                            biker.setLicensePlate(requestAlloted.bikerLocation.bike_license_plate);
                            biker.setGcmToken(requestAlloted.bikerLocation.biker_device_token);
                            biker.setPhNo(requestAlloted.bikerLocation.phone);
                            trip.setBikerAlloted(biker);
                            mRealm.beginTransaction();
                            mRealm.copyToRealmOrUpdate(trip);
                            mRealm.commitTransaction();
                            //Toast.makeText(this, "Request assigned to biker:" + requestAlloted.bikerLocation.first_name, Toast.LENGTH_LONG).show();
                            // GOTO TripActivity
                            SharedPrefHelper.setKey_isOnTrip(this, true);
                            Intent intent = new Intent(this, TripActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            hideProgress();
                            //Starts a linear repeated alarm that sends a broadcast to Play Services,
                            // which in turn sends a heartbeat to GServices.
                            Pacemaker.scheduleLinear(this, Constants.GCM_HEARTBEAT_INTERVAL);
                            break;
                        case Constants.ResponseStatusCode.NO_BIKERS_AVAILABLE:
                            requestFailedSetup(R.string.error_bikerallocated);
                            break;
                        case Constants.ResponseStatusCode.FAILED:
                            requestFailedSetup(R.string.error_serverissue);
                            break;
                    }
                    break;
            }

        }catch (IOException e) {
            //Send log to dev on this crash
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "HTTPRequest of TYPE: " + REQUESTTYPE);
            Crashlytics.logException(e);
            e.printStackTrace();
            requestFailedSetup(R.string.error_sendrequesterror);
        }
        catch (Exception e) {
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "HTTPRequest of TYPE: " + REQUESTTYPE);
            Crashlytics.logException(e);
            e.printStackTrace();
            requestFailedSetup(R.string.error_sendrequesterror);
        }
    }

    public void refreshBikerMarkers(List<BikerLocation> bikerLocations) {
        makeTripBtn.setEnabled(true);
        mMap.clear();
        for (BikerLocation bikerLocation : bikerLocations) {
            LatLng latLng = new LatLng(bikerLocation.current_lat, bikerLocation.current_long);
            mMap.addMarker(new MarkerOptions().position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.biker_mapmarker)));
        }
    }

    private void startRequestSetup() {
        //mProgressBar.setVisibility(View.VISIBLE);
        showProgress();
        makeTripBtn.setEnabled(false);
    }

    private void requestFailedSetup(int stringId) {
        Toast.makeText(this, getString(stringId), Toast.LENGTH_LONG).show();
        //mProgressBar.setVisibility(View.GONE);
        hideProgress();
        makeTripBtn.setEnabled(false);
    }

    private void showProgress() {
        mProgressBar= CommonUtilities.getProgressBar(this, "Making A Request");
        mMainFrame.addView(mProgressBar);
    }

    private void hideProgress() {
        mMainFrame.removeView(mProgressBar);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.i(LOG_TAG, "Zoom level is :" + cameraPosition.zoom);
        CURRENT_MAP_ZOOM_LEVEL = cameraPosition.zoom;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }
}//End of Class
