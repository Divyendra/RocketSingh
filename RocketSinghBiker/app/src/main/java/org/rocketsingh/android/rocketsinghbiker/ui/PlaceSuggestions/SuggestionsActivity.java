package org.rocketsingh.android.rocketsinghbiker.ui.PlaceSuggestions;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.rocketsingh.android.rocketsinghbiker.R;
import org.rocketsingh.android.rocketsinghbiker.realmmodels.Trip;
import org.rocketsingh.android.rocketsinghbiker.ui.BikerMap.BikerMapActivity;
import org.rocketsingh.android.rocketsinghbiker.utilities.GoogleApiClientHelperActivity;
import org.rocketsingh.android.rocketsinghbiker.ui.PlaceSuggestions.PlaceAutocompleteAdapter_Custom_RESTFUL.PlaceAutocomplete;
import org.rocketsingh.android.rocketsinghbiker.utilities.SharedPrefHelper;

import io.realm.Realm;

/**
 * Created by Divyu on 7/7/2015.
 */
public class SuggestionsActivity extends GoogleApiClientHelperActivity {

    private static final String LOG_TAG = SuggestionsActivity.class.getSimpleName() ;

    private EditText mPlaceSearchEditText;
    private ListView mSuggestionsListView;

    private PlaceAutocompleteAdapter_Custom_RESTFUL mAdapter;

    private static final LatLngBounds BOUNDS_INDIA = new LatLngBounds(
            new LatLng(8.456589, 69.419848), new LatLng(36.778910, 96.050708));

    Realm mRealm;
    Trip mTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_place);

        mPlaceSearchEditText = (EditText) findViewById(R.id.placeSearchBar);
        mSuggestionsListView = (ListView) findViewById(R.id.placeSuggestionsListView);
        mAdapter = new PlaceAutocompleteAdapter_Custom_RESTFUL(this);
        mSuggestionsListView.setAdapter(mAdapter);

        long timeID = getIntent().getLongExtra(BikerMapActivity.intentextrakey_triptimeid, 0L);
        if (timeID == 0L) finish();

        mRealm = Realm.getInstance(this);
        mTrip = mRealm.where(Trip.class).equalTo("timeId", timeID).findFirst();

        mPlaceSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence textEntered, int start, int before, int count) {
                if (textEntered.length() > 0) {
                    mAdapter.getFilter().filter(textEntered);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSuggestionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(LOG_TAG, "ItemClick detected");
                PlaceAutocompleteAdapter_Custom_RESTFUL adapter =
                        (PlaceAutocompleteAdapter_Custom_RESTFUL)parent.getAdapter();
                PlaceAutocomplete place = adapter.getItem(position);
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, place.placeId);
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            }
        });
    }

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);
            LatLng latLng = place.getLatLng();
            Log.i(LOG_TAG, "lat,long : (" + latLng.latitude + ", " + latLng.longitude + ")");
            mRealm.beginTransaction();
            mTrip.setEndLatitude(latLng.latitude);
            mTrip.setEndLongitude(latLng.longitude);
            //Note:End point will be looked in onConnectionCalled() in BikerMapActivity
            mRealm.commitTransaction();
            places.release();
            SharedPrefHelper.setIsDestChanged(SuggestionsActivity.this, true);
            finish();
        }
    };//End of CallbackClass

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }
}
