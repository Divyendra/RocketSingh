package org.rocketsingh.android.rocketsinghpillion.utilities;

/**
 * Created by Divyu on 9/30/2015.
 */
public class Constants {
    //Response meaning HTTP Response.
    public static class ResponseStatusCode {
        public static final String SUCCESS = "1";
        public static final String DUPLICATE = "2";
        public static final String FAILED = "3";
        public static final String NO_BIKERS_AVAILABLE = "7";
        public static final String CONDITIONAL_UPDATE_FAILED = "8";
    }

    // NOTE : The below is named RequestStatusCode in ServerSide.
    public static class TripStatusCode {
        //TRIP_ASSIGNED is only in ClientSide(BikerApp).A status where biker is yet to start towards customer.
        public static final String TRIP_ASSIGNED = "0";
        public static final String TRIP_ACTIVE = "1";
        public static final String TRIP_STARTED = "2";
        public static final String TRIP_CANCELLED = "3";
        public static final String TRIP_COMPLETED = "4";
        public static final String TRIP_OTHER = "5";
    }

    public static class Entities {
        public static final int BIKER = 1;
        public static final int CUSTOMER = 2;
        public static final int MERCHANT = 3;
    }

    public static final int GCM_HEARTBEAT_INTERVAL = 2; //In minutes;

}
