package org.rocketsingh.android.rocketsinghbiker.utilities.DirectionsUtil;

import java.util.ArrayList;

public interface RoutingListener {
    public void onRoutingFailure();

    public void onRoutingStart();

    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex);

    public void onRoutingCancelled();
}
