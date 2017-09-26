package org.rocketsingh.android.rocketsinghpillion.utilities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Divyu on 4/2/2015.
 */
public class TaskFragment extends Fragment {

    private static final String LOG_TAG = TaskFragment.class.getSimpleName();
    //Interface needed for callbacks and sending info to the hosting Activity of this fragment.
    public interface TaskCallbacks {
        void onProgressUpdate(int percent);
        void onCancelled(String reason);
        void onPostExecute(Object result, int REQUESTTYPE);
    }

    private static final int GET = 0;
    private static final int POST_STATUS = 1; //Use when you post and expect a string status back and not a protoresult
    private static final int POST_RESULT = 2; //Use when you post and expect a protoobject in response Eg: UserLogin

    //Will store the activity object to which callback methods will be applied
    private TaskCallbacks mCallbacks;
    //Will store the Async task object
    private HttpAsyncTaskProto mTask;
    private boolean startedTask = false; //To Check if in progress
    private  int httpType;

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        mCallbacks = (TaskCallbacks) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    //REQUESTTYPE is used for differentiating multiple httprequests in a single activity
    //Send a dummy int if only HTTPtask is there in a Activity.
    public void startPostTaskProto_ResponseStatus(Object postArgs[], int REQUESTTYPE) {
        //postArgs would have params required to do a http post (like destination URL and JSON to be sent)
        mTask = new HttpAsyncTaskProto();
        httpType = POST_STATUS;
        startedTask = true;
        mTask.requestType = REQUESTTYPE;
        mTask.execute(postArgs);
    }

    //REQUESTTYPE is used for differentiating multiple httprequests in a single activity
    //Send a dummy int if only HTTPtask is there in a Activity.
    public void startPostTaskProto_ResponseProto(Object postArgs[], int REQUESTTYPE) {
        //postArgs would have params required to do a http post (like destination URL and JSON to be sent)
        mTask = new HttpAsyncTaskProto();
        httpType = POST_RESULT;
        startedTask = true;
        mTask.requestType = REQUESTTYPE;
        mTask.execute(postArgs);
    }

    //REQUESTTYPE is used for differentiating multiple httprequests in a single activity
    //Send a dummy int if only HTTPtask is there in a Activity.
    public void startGetTaskProto(Object postArgs[], int REQUESTTYPE) {
        //postArgs would have params required to do a http post (like destination URL and JSON to be sent)
        mTask = new HttpAsyncTaskProto();
        httpType = GET;
        startedTask = true;
        mTask.requestType = REQUESTTYPE;
        mTask.execute(postArgs);
    }

    //Returns true if a HTTP is already in progress
    public boolean isTaskStarted() {
        return startedTask;
    }


    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance, meaning all views in activity will not be garbage collected.
     */
    @Override
    public void onDetach() {
        // TODO Auto-generated method stub
        super.onDetach();
        mCallbacks = null;
    }

    private class HttpAsyncTaskProto extends AsyncTask<Object, Integer, String> {

        String reason; //used for cancellation
        Object responeContent;
        private int requestType;

        @Override
        protected String doInBackground(Object... params) {
            // TODO Auto-generated method stub
            HttpClient httpclient = null;
            String destinationURL = (String)params[0];  //"http://192.168.0.190:8809/mobileTest";
            Log.i(LOG_TAG, "Server address : " + params[0]);
            if (!Fabric.isInitialized()) {
                Fabric.with(getActivity(), new Crashlytics());
            }
            try {
                httpclient = new DefaultHttpClient();
                HttpParams httpParams = httpclient.getParams();
                int timeout = 10; //Unit in seconds
                //Connection Timeout (http.connection.timeout) – the time to establish the connection with the remote host
                httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout * 1000);
                //Socket Timeout (http.socket.timeout) – the time waiting for data – after the connection was established; maximum time of inactivity between two data packets
                httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout * 1000);

                HttpResponse response = null;
                switch (httpType) {
                    case GET:
                        HttpGet get = new HttpGet(destinationURL);
                        response = httpclient.execute(get);
                        responeContent = EntityUtils.toByteArray(response.getEntity());
                        break;
                    case POST_STATUS: {
                        HttpPost httpPost = new HttpPost(destinationURL);
                        ByteArrayEntity byteArrayEntity = new ByteArrayEntity((byte[]) params[1]);
                        byteArrayEntity.setContentType("application/octet-stream");
                        httpPost.setEntity(byteArrayEntity);
                        response = httpclient.execute(httpPost);
                        responeContent = EntityUtils.toString(response.getEntity());
                    }
                        break;
                    case POST_RESULT: {
                        HttpPost httpPost = new HttpPost(destinationURL);
                        ByteArrayEntity byteArrayEntity = new ByteArrayEntity((byte[]) params[1]);
                        byteArrayEntity.setContentType("application/octet-stream");
                        httpPost.setEntity(byteArrayEntity);
                        response = httpclient.execute(httpPost);
                        responeContent = EntityUtils.toByteArray(response.getEntity());
                    }
                        break;
                }

                int statuscode = response.getStatusLine().getStatusCode();
                if ( statuscode == 200) {
                    //SUCCESS
                    return "success";
                } else {
                    //Issue with Network reaching Intruo.
                    reason = "Issue reaching Server.We are on it";
                    this.cancel(true);
                }

            } catch(Exception e) {
                reason = "Issue sending details. Log has been sent to check Issue";
                try {
                    Crashlytics.setString("WHERE", LOG_TAG);
                    Crashlytics.setString("MESSAGE", "Type is :" + requestType);
                    Crashlytics.logException(e);
                } catch (IllegalStateException ce) {
                    Log.i(LOG_TAG, "Crashyltics Exception");
                }
                e.printStackTrace();
                if (mCallbacks != null) {
                    //onCancelled method is in Activity but it is run on this thread and not on that Activity's UI thread. So used this.runOnUiThread(new Runnable() { } );
                    mCallbacks.onCancelled(reason);
                }
            } finally {
                httpclient.getConnectionManager().shutdown();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            if (mCallbacks != null) {
                mCallbacks.onProgressUpdate(values[0]);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            startedTask = false;
            if(result == "success") {
                if (mCallbacks != null) {
                    mCallbacks.onPostExecute(responeContent, requestType);
                }
            }
        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            startedTask = false;
            if (mCallbacks != null) {
                mCallbacks.onCancelled(reason);
            }
        }
    }//End of Async Class for ProtoBuffers

}
