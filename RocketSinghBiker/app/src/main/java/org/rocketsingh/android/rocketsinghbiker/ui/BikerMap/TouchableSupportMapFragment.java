package org.rocketsingh.android.rocketsinghbiker.ui.BikerMap;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by Divyu on 10/2/2015.
 */
public class TouchableSupportMapFragment extends SupportMapFragment {
    public View mOriginalMapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mOriginalMapView = super.onCreateView(inflater, container, savedInstanceState);
        TouchableWrapper touchableWrapper = new TouchableWrapper(getActivity());
        touchableWrapper.addView(mOriginalMapView);
        return touchableWrapper;
    }

    @Nullable
    @Override
    public View getView() {
        return mOriginalMapView;
    }
}
