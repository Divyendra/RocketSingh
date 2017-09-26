package org.rocketsingh.android.rocketsinghbiker.ui.BikerMap;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.rocketsingh.android.rocketsinghbiker.R;
import org.rocketsingh.android.rocketsinghbiker.realmmodels.Trip;
import org.rocketsingh.android.rocketsinghbiker.service.LocationUpdationService;
import org.rocketsingh.android.rocketsinghbiker.service.TripUpdationService;
import org.rocketsingh.android.rocketsinghbiker.ui.PlaceSuggestions.SuggestionsActivity;
import org.rocketsingh.android.rocketsinghbiker.ui.navdrawer.RSDrawerActivitywithGoogleApi;
import org.rocketsingh.android.rocketsinghbiker.utilities.CommonUtilities;
import org.rocketsingh.android.rocketsinghbiker.utilities.Constants;
import org.rocketsingh.android.rocketsinghbiker.utilities.DirectionsUtil.AbstractRouting;
import org.rocketsingh.android.rocketsinghbiker.utilities.DirectionsUtil.Route;
import org.rocketsingh.android.rocketsinghbiker.utilities.DirectionsUtil.Routing;
import org.rocketsingh.android.rocketsinghbiker.utilities.DirectionsUtil.RoutingListener;
import org.rocketsingh.android.rocketsinghbiker.utilities.SharedPrefHelper;
import org.rocketsingh.android.rocketsinghbiker.utilities.TaskFragment;

import java.util.ArrayList;
import org.rocketsingh.android.rocketsinghbiker.utilities.Constants.TripStatusCode;

import io.fabric.sdk.android.Fabric;
import io.realm.exceptions.RealmMigrationNeededException;
import mobilerequest.RequestData;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;


/********NOTE********
 * CameraChangeListener of Maps is not useful for detecting drags on UI, so using customhack'TouchableWrapper' here.
 */
public class BikerMapActivity extends RSDrawerActivitywithGoogleApi implements TouchableWrapper.UpdateMapAfterUserInterection, TaskFragment.TaskCallbacks, GoogleMap.OnCameraChangeListener, RoutingListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String LOG_TAG = BikerMapActivity.class.getSimpleName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Button mRespondActionBtn; //Button which acts as StartTrip, EndTrip etc.. any action of the biker.
    private Button mShowCustDetailsBtn;
    private LinearLayout mButtonsLayout;
    private LatLng mLastLatLng;
    protected LocationRequest mLocationRequest;
    private Marker bikerMarker;
    //private static String mCurrentTripState;
    Animation btnslideOut, btnslideIn;

    private TaskFragment mTaskFragment;
    private static final String TAG_TASK_FRAGMENT = "task_fragment";

    private Realm mRealm;
    private boolean isRealmOpen;
    MyRealmChangeListener mRealmListener;
    RealmResults<Trip> ongoingTrips;

    private static final float DEFAULT_MAP_ZOOM_LEVEL = 15;
    private static float CURRENT_MAP_ZOOM_LEVEL = DEFAULT_MAP_ZOOM_LEVEL;
    private static final int POLYLINE_WIDTH = 12;
    private Polyline mPolyline;

    public static final String NOTIFYENDTRIP_INTENTACTION = "BikerMapActivityNotifyTripEnd";
    private final EndTripStatusReceiver mEndTripStatusReceiver = new EndTripStatusReceiver();
    public static final String intentextrakey_tripendstatus = "tripendstatus";
    public static final String intentextrakey_triptimeid = "triptimeid";
    //Above intent used for sending timeids in two places:
    //a)TripUpdationService to BikerMapAct's receiver once Trip is ended
    //b)BikerMapAct to SuggestionsAct to send timeid to update destlat/long.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mButtonsLayout = (LinearLayout) findViewById(R.id.buttonsLayout);
        mRespondActionBtn = (Button) mButtonsLayout.findViewById(R.id.respondActionBtn);
        mShowCustDetailsBtn = (Button) mButtonsLayout.findViewById(R.id.showCustDetailsBtn);
        try {
            mRealm = Realm.getInstance(this);
            isRealmOpen = true;
        } catch (RealmMigrationNeededException e) {
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.logException(e);
            Log.i(LOG_TAG, "Deleting Realm files instead of migrating");
            Realm.deleteRealmFile(BikerMapActivity.this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRealmListener = new MyRealmChangeListener();
        mRealm.addChangeListener(mRealmListener);
        ongoingTrips = mRealm.where(Trip.class).equalTo("status", TripStatusCode.TRIP_ASSIGNED)
                .or().equalTo("status", TripStatusCode.TRIP_ACTIVE)
                .or().equalTo("status", TripStatusCode.TRIP_STARTED).findAll();

        if (ongoingTrips.size() >= 1 ) {
            Log.i(LOG_TAG, "BIKER ONTRIP");
            //Start Trip Updation service only only if it is still not in ASSIGNED state
            if (!ongoingTrips.get(0).getStatus().equals(TripStatusCode.TRIP_ASSIGNED)) {
                if (!TripUpdationService.isServiceRunning()) {
                    Log.i(LOG_TAG, "Starting TripUpdationService");
                    Intent tripupdationServiceIntent = new Intent(this, TripUpdationService.class);
                    startService(tripupdationServiceIntent);
                }
            }
        } else {
            //Start Location Updation service
            Log.i(LOG_TAG, "BIKER HAS NO TRIPS");
            if (!LocationUpdationService.isServiceRunning()) {
                Log.i(LOG_TAG, "Starting LocationUpdationService");
                startLocationUpdationService();
            }
        }
    }

    @Override
    public void onConnectionCalled() {
        if (mMap != null) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastLocation != null) {
                mLastLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                if (bikerMarker != null) bikerMarker.remove(); //Validation to avoid multiple markers on map

                bikerMarker = mMap.addMarker(new MarkerOptions().position(mLastLatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.biker_mapmarker)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLatLng, CURRENT_MAP_ZOOM_LEVEL));
                startTripifNeeded();
            }
        }
    }

    private void setUpDirections(LatLng startLatLng, LatLng destLatLng) {
        Log.i(LOG_TAG, "Called setUpDirections() from (" + startLatLng.latitude + ", " + startLatLng.longitude
                + ") -> (" + destLatLng.latitude + ", " + destLatLng.longitude + ")");
        Routing routing = new Routing.Builder()
                .withAPIKEY(getString(R.string.google_directions_key))
                .waypoints(startLatLng, destLatLng)
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this).build();
        routing.execute();
    }

    private void startTripifNeeded() {
        if (ongoingTrips.size() != 0) {
            btnslideIn =  AnimationUtils.loadAnimation(this, R.anim.btnslidein);
            btnslideOut =  AnimationUtils.loadAnimation(this, R.anim.btnslideout);
            Trip trip = ongoingTrips.get(0);
            String currentTripState = trip.getStatus();
            if (trip.getStatus().equals(TripStatusCode.TRIP_ACTIVE) ||
                    trip.getStatus().equals(TripStatusCode.TRIP_ASSIGNED)) {
                double custLat = trip.getCustomerLatitude();
                double custLong = trip.getCustomerLongitude();
                LatLng custLatLng = new LatLng(custLat, custLong);
                setUpDirections(mLastLatLng, custLatLng);
            } else if (trip.getStatus().equals(TripStatusCode.TRIP_STARTED)) {
                if (trip.getEndLatitude() != 0.0 || trip.getEndLongitude() != 0.0) {
                    LatLng start = new LatLng(trip.getStartLatitude(), trip.getStartLongitude());
                    LatLng end = new LatLng(trip.getEndLatitude(), trip.getEndLongitude());
                    setUpDirections(start, end);
                }
            }
            //Making Buttons Visible
            setActionButton(currentTripState);
        }
    }

    private void setActionButton(String currentTripState) {
        mButtonsLayout.setVisibility(View.VISIBLE);
        switch (currentTripState) {
            case TripStatusCode.TRIP_ASSIGNED:
                mRespondActionBtn.setText(getString(R.string.btn_respondaction_newtripreceived));
                break;
            case TripStatusCode.TRIP_ACTIVE:
                mRespondActionBtn.setText(getString(R.string.btn_respondaction_towardscust));
                break;
            case TripStatusCode.TRIP_STARTED:
                mRespondActionBtn.setText(getString(R.string.btn_respondaction__towardscustdestination));
                break;
        }
    }

    private void animateButton(int status_R_id) {
        mRespondActionBtn.startAnimation(btnslideOut);
        mRespondActionBtn.setText(getString(status_R_id));
        mRespondActionBtn.startAnimation(btnslideOut);
    }

    public void clickedActionButton(View v) {
        String currentTripState = ongoingTrips.get(0).getStatus();
        switch (currentTripState) {
            case TripStatusCode.TRIP_ASSIGNED: //Changing from  ASSIGNED -> ACTIVE
                Log.i(LOG_TAG, "Changing from  ASSIGNED -> ACTIVE");
                //Changing ASSIGNED->ACTIVE in Realm and Start TripUpdationService
                mRealm.beginTransaction();
                ongoingTrips.get(0).setStatus(TripStatusCode.TRIP_ACTIVE);
                mRealm.commitTransaction();
                animateButton(R.string.btn_respondaction_towardscust);
                Intent stopIntent = new Intent(this, LocationUpdationService.class);
                stopService(stopIntent);
                Intent intent = new Intent(this, TripUpdationService.class);
                startService(intent);
                break;
            case TripStatusCode.TRIP_ACTIVE:   //Changing from  ACTIVE -> STARTED
                Log.i(LOG_TAG, "Changing from  ACTIVE -> STARTED");
                //Make serverdb & localdb(InPostExecute) change from status ACTIVE to STARTED
                //SetInserttype to TRIP_STARTED in SharedPref just before localdb change so that TripUpdationService detects
                //Also save current lat,long to Trip table in Realm
                Object[] postArgs = new Object[2];
                postArgs[0] = getString(R.string.server_address) + getString(R.string.path_requestUpdate);
                Trip trip = ongoingTrips.get(0);
                RequestData requestData = new RequestData.Builder()
                        .entity_id(trip.getEntityId())
                        .entity_type(Integer.parseInt(trip.getEntityType()))
                        .time_id(String.valueOf(trip.getTimeId()))
                        .request_status_flag(TripStatusCode.TRIP_STARTED).build();
                postArgs[1] = requestData.toByteArray();
                mTaskFragment.startPostTaskProto_ResponseStatus(postArgs);
                break;
            case TripStatusCode.TRIP_STARTED:  //Changing from STARTED -> FINISHED
                //Lazy coding.Registering just before it could be of use.
                IntentFilter filter = new IntentFilter(NOTIFYENDTRIP_INTENTACTION);
                LocalBroadcastManager.getInstance(this).registerReceiver(mEndTripStatusReceiver, filter);
                Log.i(LOG_TAG, "Changing from STARTED -> FINISHED");
                //Make local and server change from status STARTED to FINISHED
                //Send an intent to TripUpdationService to send statusupdate and tripdata to server and update local db
                Intent endTripIntent = new Intent(this, TripUpdationService.class);
                endTripIntent.setAction(TripUpdationService.END_TRIP_ACTION_INTENT);
                startService(endTripIntent);
                //OnReceiving a success from server -> service -> updatelocal db -> this activity -> show payment dialog box.
                break;
        }
    }

    //To get latest biker latitude and longitude via updation in services(LocationUpdationService or TripUpdationService)
    //This is to avoid double location requests in the same app.
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String pref_lat_key = getString(R.string.pref_key_currentlat);
        String pref_long_key = getString(R.string.pref_key_currentlong);
        if (key.equals(pref_lat_key) || key.equals(pref_long_key)) {
            double bikerLat = Double.valueOf(sharedPreferences.getString(pref_lat_key, "0.0"));
            double bikerLong = Double.valueOf(sharedPreferences.getString(pref_long_key, "0.0"));
            if (bikerLat == 0.0 || bikerLong == 0.0) return;
            mLastLatLng = new LatLng(bikerLat, bikerLong);
            if (bikerMarker == null) {
                bikerMarker = mMap.addMarker(new MarkerOptions().position(mLastLatLng)
                     .icon(BitmapDescriptorFactory.fromResource(R.drawable.biker_mapmarker)));
            } else {
                bikerMarker.setPosition(mLastLatLng);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLatLng, CURRENT_MAP_ZOOM_LEVEL));
        }
    }

    class MyRealmChangeListener implements RealmChangeListener {
         @Override
         public void onChange() {
             Log.i(LOG_TAG, "Realm onChange() detected");
             if (SharedPrefHelper.Realm_Trips.getInsertType(BikerMapActivity.this) == SharedPrefHelper.Realm_Trips.NEWTRIP) {
                 //TODO: Blink the screen once u get free time
                 Log.i(LOG_TAG, "New trip detected");
                 startTripifNeeded();
                 SharedPrefHelper.Realm_Trips.setInsertType(BikerMapActivity.this, -1);
             } else if (SharedPrefHelper.Realm_Trips.getInsertType(BikerMapActivity.this)
                     == SharedPrefHelper.Realm_Trips.TRIP_CANCELLEED) {
                 AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BikerMapActivity.this, R.style.Base_Theme_AppCompat_Dialog_Alert);
                 dialogBuilder.setTitle("NOTICE");
                 dialogBuilder.setMessage("The Trip has been CANCELLED by the customer");
                 dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             dialog.dismiss();
                         }
                     }).show();
                 afterEndTripSetup();
             }
         }
     }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        //To remove previous polylines
        if (mPolyline != null) mPolyline.remove();
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(getResources().getColor(android.R.color.holo_blue_dark));
        polyOptions.width(POLYLINE_WIDTH);
        try {
            polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
        } catch (IndexOutOfBoundsException e) {
            Crashlytics.setString("WHERE", LOG_TAG);
            Crashlytics.setString("MESSAGE", "Error while fetching Directions");
            Crashlytics.logException(e);
            Log.i(LOG_TAG, "Error fetching Directions");
            e.printStackTrace();
        }
        mPolyline = mMap.addPolyline(polyOptions);
    }

    @Override
    public void onPostExecute(Object result) {
        switch ((String)result) {
            case Constants.ResponseStatusCode.SUCCESS: //Updating in LocalDB ACTIVE -> STARTED
                Log.i(LOG_TAG, "ACTIVE -> STARTED result: Success");
                //SetInserttype to TRIP_STARTED in SharedPref just before localdb change so that TripUpdationService detects
                SharedPrefHelper.Realm_Trips.setInsertType(this, SharedPrefHelper.Realm_Trips.TRIP_STARTED);
                mRealm.beginTransaction();
                ongoingTrips.get(0).setStatus(TripStatusCode.TRIP_STARTED);
                ongoingTrips.get(0).setStartLatitude(mLastLatLng.latitude);
                ongoingTrips.get(0).setStartLongitude(mLastLatLng.longitude);
                mRealm.commitTransaction();
                if (mPolyline != null) mPolyline.remove();
                animateButton(R.string.btn_respondaction__towardscustdestination);
                invalidateOptionsMenu(); //To Refresh MenuItems in Toolbar.
                break;
            case Constants.ResponseStatusCode.FAILED:
                Toast.makeText(getApplicationContext(), "Failed to Start Trip.", Toast.LENGTH_SHORT).show();
                break;
        }
        //Stop Progressbar here if added
    }

    private class EndTripStatusReceiver extends BroadcastReceiver{
        private final String LOG_TAG = EndTripStatusReceiver.class.getSimpleName();
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean status = intent.getBooleanExtra(intentextrakey_tripendstatus, false);
            long timeId = intent.getLongExtra(intentextrakey_triptimeid, 0L);
            if (timeId != 0L) {
                if (status) {
                    //TODO: Show Payment Dialog
                    Trip trip = mRealm.where(Trip.class).equalTo("timeId", timeId).findFirst();
                    final AppCompatDialog dialog = new AppCompatDialog(BikerMapActivity.this, R.style.DialogStyle);
                    dialog.setTitle("Trip Details");
                    dialog.setContentView(R.layout.dialog_tripdetails);
                    TextView tvCost = (TextView)dialog.findViewById(R.id.tripCost);
                    TextView tvDistance = (TextView)dialog.findViewById(R.id.tripDistance);
                    TextView tvTime = (TextView)dialog.findViewById(R.id.tripTime);
                    tvCost.setText(getString(R.string.format_amount_INR, trip.getTripCost()));
                    tvDistance.setText(getString(R.string.format_distance, trip.getTripDistance()/1000));
                    tvTime.setText(getString(R.string.format_time, trip.getTripTime()/60000));
                    Button okBtn = (Button)dialog.findViewById(R.id.okBtn);
                    okBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();

                    afterEndTripSetup();
                    //TODO: Hide ProgressBar
                    return;
                } else {
                    Log.i(LOG_TAG, "Status of ENdtrip: " + status);
                }
            } else {
                Log.i(LOG_TAG, "TimeID is 0");
            }
            //TODO: Hide ProgressBar
            //TODO:Show Failure message
            Toast.makeText(getApplicationContext(), "Unable to Finish Trip. Try again", Toast.LENGTH_LONG).show();
        }
    }

    //Modularizing the code to be run after enf trip is successful
    private void afterEndTripSetup() {
        //Remove Polylines on Map
        if (mPolyline != null) mPolyline.remove();
        mButtonsLayout.setVisibility(View.INVISIBLE);
        //To remove search action button
        invalidateOptionsMenu();
        //Unregistering once trip is finished
        LocalBroadcastManager.getInstance(BikerMapActivity.this).unregisterReceiver(mEndTripStatusReceiver);
        //Starting LocationUpdation again
        if (!LocationUpdationService.isServiceRunning()) {
            Log.i(LOG_TAG, "Starting LocationUpdationService");
            startLocationUpdationService();
        }
    }

    public void showCustDetails(View v) {
        if (ongoingTrips.size() >= 1) {
            final Trip trip = ongoingTrips.get(0);
            final AppCompatDialog dialog = new AppCompatDialog(this);
            dialog.setTitle("Customer Details");
            dialog.setContentView(R.layout.dialog_custdetails);
            TextView tcCustName = (TextView)dialog.findViewById(R.id.custName);
            TextView tcCustPhno = (TextView)dialog.findViewById(R.id.custPhNo);
            Button callCustBtn = (Button)dialog.findViewById(R.id.callCustomer);
            tcCustName.setText(trip.getCustomerName());
            tcCustPhno.setText(trip.getEntityId());
            Button okBtn = (Button)dialog.findViewById(R.id.okBtn);
            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            callCustBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callDriver = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + trip.getEntityId()));
                    startActivity(callDriver);
                }
            });
            dialog.show();
        }
    }

    private void startLocationUpdationService() {
        if (CommonUtilities.getConnectivityStatus(BikerMapActivity.this)) {
            Intent locupdationServiceIntent = new Intent(BikerMapActivity.this, LocationUpdationService.class);
            startService(locupdationServiceIntent);
        } else {
            Toast.makeText(this, "Internet is not present to be active", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onPause() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setUpMapIfNeeded() {// Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {// Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            mMap.setMyLocationEnabled(true);
            mMap.setOnCameraChangeListener(this); //For knowing zoom change and the current zoom.
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bikermap, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_searchDestination) {
            //Going to placesearch activity
            Intent intent = new Intent(this, SuggestionsActivity.class);
            intent.putExtra(intentextrakey_triptimeid, ongoingTrips.get(0).getTimeId());
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isRealmOpen && ongoingTrips!= null && ongoingTrips.size() >= 1) {
            if (ongoingTrips.get(0).getStatus().equals(TripStatusCode.TRIP_STARTED)) {
                menu.findItem(R.id.action_searchDestination).setVisible(true);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCancelled(String reason) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "Failed to Update", Toast.LENGTH_SHORT).show();
                //Stop Progressbar here if added
            }
        });
    }

    @Override
    public void onRoutingFailure() {
        Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.i(LOG_TAG, "Zoom level is :" + cameraPosition.zoom);
        CURRENT_MAP_ZOOM_LEVEL = cameraPosition.zoom;
    }

    @Override
    public void onRoutingStart() { }

    @Override
    public void onRoutingCancelled() { }

    //The Map has been moved and hence the marker on the map.
    @Override
    public void onUpdateMapAfterUserInterection() {//To get center now => mMap.getCameraPosition().target;
    }

    @Override
    public void onProgressUpdate(int percent) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.removeAllChangeListeners();
        mRealm.close();
        isRealmOpen = false;
    }
}//End of Class
