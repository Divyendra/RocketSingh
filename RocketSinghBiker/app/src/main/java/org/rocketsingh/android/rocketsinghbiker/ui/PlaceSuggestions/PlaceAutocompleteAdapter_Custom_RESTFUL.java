/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.rocketsingh.android.rocketsinghbiker.ui.PlaceSuggestions;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rocketsingh.android.rocketsinghbiker.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Adapter that handles Autocomplete requests from the Places Geo Data API.
 * Results are encoded as {@link PlaceAutocompleteAdapter_Custom_RESTFUL.PlaceAutocomplete}
 * objects
 * that contain both the Place ID and the text longName from the autocomplete query.
 * <p>
 * Note that this adapter requires a valid {@link GoogleApiClient}.
 * The API client must be maintained in the encapsulating Activity, including all lifecycle and
 * connection states. The API client must be connected with the {@link Places#GEO_DATA_API} API.
 */
public class PlaceAutocompleteAdapter_Custom_RESTFUL
        extends ArrayAdapter<PlaceAutocompleteAdapter_Custom_RESTFUL.PlaceAutocomplete> implements Filterable {

    private static final String TAG = "PlaceAutocompleteAdapter";
    /**
     * Current results returned by this adapter.
     */
    private ArrayList<PlaceAutocomplete> mResultList;

    private LayoutInflater layoutInflater;

    private static final String PLACES_AUTOCOMPLETE_BASEURL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
    private static final String URI_PARAM_TYPES = "types";
    private static final String URI_PARAM_APIKEY = "key";
    private static final String URI_PARAM_SEARCHKEY = "input";
    private static final String APIKEY_VALUE = "AIzaSyD5AG4-Li5vqOaIqLIme_7kDoHh9otXCxM";
    private static final String TYPES_VALUE = "geocode";

    public PlaceAutocompleteAdapter_Custom_RESTFUL(Context context) {
        super(context, 0);
        layoutInflater = LayoutInflater.from(context);
    }

    /**
     * Returns the filter for the current set of autocomplete results.
     */
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                // Skip the autocomplete query if no constraints are given.
                if (constraint != null) {
                    // Query the autocomplete API for the (constraint) search string.
                    mResultList = getAutocomplete(constraint);
                    if (mResultList != null) {
                        // The API successfully returned results.
                        results.values = mResultList;
                        results.count = mResultList.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    clear();
                    addAll((ArrayList<PlaceAutocomplete>) results.values);
                    //notifyDataSetChanged();
                } else {
                    // The API did not return any results, invalidate the data set.
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    /**
     * Submits an autocomplete query to the Places Geo Data Autocomplete API.
     * Results are returned as {@link PlaceAutocompleteAdapter_Custom_RESTFUL.PlaceAutocomplete}
     * objects to store the Place ID and longName that the API returns.
     * Returns an empty list if no results were found.
     * Returns null if the API client is not available or the query did not complete
     * successfully.
     * This method MUST be called off the main UI thread, as it will block until data is returned
     * from the API, which may include a network request.
     *
     * @param constraint Autocomplete query string
     * @return Results from the autocomplete API or null if the query was not successful.
     * @see Places#GEO_DATA_API#getAutocomplete(CharSequence)
     */
    private ArrayList<PlaceAutocomplete> getAutocomplete(CharSequence constraint) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String responseJSON = null;

        try {
            Uri builtUri = Uri.parse(PLACES_AUTOCOMPLETE_BASEURL).buildUpon()
                    .appendQueryParameter(URI_PARAM_TYPES, TYPES_VALUE)
                    .appendQueryParameter(URI_PARAM_APIKEY, APIKEY_VALUE)
                    .appendQueryParameter(URI_PARAM_SEARCHKEY, constraint.toString())
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            android.util.Log.i("Sunshine", "HTTP Rescode: " + urlConnection.getResponseCode());

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            android.util.Log.i("Test", "Res code:" + urlConnection.getResponseCode());
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                android.util.Log.i("Sunshine", "InputStream is null .Returning");
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                android.util.Log.i("Sunshine", "Buffer is null .Returning");
                return null;
            }
            responseJSON = buffer.toString();

            return parseJson(responseJSON);

        } catch(MalformedURLException e) {
            Log.e(TAG, "URL constructed is malformed.");
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Error fetching automcomplete resukts from Places API. Trace :  " + e);
            return null;
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PlaceAutocompleteAdapter_Custom_RESTFUL.PlaceAutocomplete itemToDisplay = getItem(position);
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listitem_autocomplete, parent, false);
        }
        TextView shortNameTextView = (TextView)convertView.findViewById(R.id.textview_locationshortname);
        TextView longNameTextView = (TextView)convertView.findViewById(R.id.textview_locationlongname);
        shortNameTextView.setText(itemToDisplay.shortName);
        longNameTextView.setText(itemToDisplay.longName);
        return convertView;
    }

    private ArrayList<PlaceAutocomplete> parseJson(String inputJSONString) {
        ArrayList<PlaceAutocomplete> placeAutocompleteList = new ArrayList<>();
        try {
            JSONObject inputJSON = new JSONObject(inputJSONString);
            JSONArray predictions = inputJSON.getJSONArray("predictions");
            JSONObject eachPrediction = null;
            for (int i=0,n=predictions.length(); i < n; i++) {
                eachPrediction = predictions.getJSONObject(i);
                placeAutocompleteList.add(new PlaceAutocomplete( eachPrediction.getString("place_id"), eachPrediction.getString("description")));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON data " + e.getMessage());
            return null;
        }
        return placeAutocompleteList;
    }

    /**
     * Holder for Places Geo Data Autocomplete API results.
     */
    class PlaceAutocomplete {
        public String placeId;
        public String longName;
        public String shortName;
        PlaceAutocomplete(String placeId, String description) {
            this.placeId = placeId;
            this.longName = description;
            int i = longName.indexOf(",");
            if (i != -1) {
                shortName = longName.substring(0, i);
            }
        }
        @Override
        public String toString() {
            int i = longName.indexOf(",");
            if (i != -1) return longName.substring(0, i);
            else return longName;
        }
    }

}
