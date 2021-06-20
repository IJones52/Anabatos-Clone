package edu.uw.tcss450.team_5_tcss_450.ui.connections;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.logging.ConsoleHandler;

import edu.uw.tcss450.team_5_tcss_450.MainActivity;
import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.home.HomeFragment;
import edu.uw.tcss450.team_5_tcss_450.io.RequestQueueSingleton;
import edu.uw.tcss450.team_5_tcss_450.notifications.Notification;
import edu.uw.tcss450.team_5_tcss_450.notifications.NotificationListViewModel;

/**
 * The view model for all of the connections based classes, handles all web service calls
 * @author Ismael Jones
 * @version 5/4/21
 * */
public class ConnectionsViewModel extends AndroidViewModel {

    /**
     * User ids
     * */
    private MutableLiveData<Integer> userID;
    private MutableLiveData<Integer> secondID;

    /**
     * Different types of connection lists
     */
    private MutableLiveData<List<Connection>> mConnectionsList;
    private MutableLiveData<List<Connection>> mRequestsList;
    private MutableLiveData<List<Connection>> mSentRequestsList;
    private MutableLiveData<List<Connection>> mSearchList;

    /**
     * Response in case of error
     * */
    private MutableLiveData<String> mResponse;

    /**
     * Base endpoint urls
     */
    private String connectionsUrl = "https://team-5-tcss-450.herokuapp.com/connections";
    private String connectionRequestsUrl = "https://team-5-tcss-450.herokuapp.com/request";
    private String infoUrl = "https://team-5-tcss-450.herokuapp.com/info";

    /**
     * An enum for selecting which list to handle
     */
    private enum Lists {
        CONNECTIONS,
        REQUESTS,
        SENT,
        SEARCH
    }
    //Set the default list location
    private Lists list = Lists.CONNECTIONS;

    /** Notification ViewModel variable */
    public NotificationListViewModel mNotiModel;


    /**
     * Initialize a Connections view model and all its lists as empty
     *
     * @param application ...
     */
    public ConnectionsViewModel(@NonNull Application application) {
        super(application);
        mConnectionsList = new MutableLiveData<>();
        mConnectionsList.setValue(new ArrayList<>());
        mRequestsList = new MutableLiveData<>();
        mRequestsList.setValue(new ArrayList<>());
        mSentRequestsList = new MutableLiveData<>();
        mSentRequestsList.setValue(new ArrayList<>());
        mSearchList = new MutableLiveData<>();
        mSearchList.setValue(new ArrayList<>());
        userID = new MutableLiveData<>();
        userID.setValue(new Integer(0));
        secondID = new MutableLiveData<>();
        secondID.setValue(new Integer(0));
        mResponse = new MutableLiveData<>();
        mResponse.setValue("");
    }

    /**
     * A method to add an observer to the list of user connections
     *
     * @param owner    the view's owner
     * @param observer the which observes changes
     */
    public void addConnectionListObserver(@NonNull LifecycleOwner owner, @NonNull Observer<? super List<Connection>> observer) {
        mConnectionsList.observeForever( observer);
    }

    /**
     * A method to add an observer to the list of user requests
     *
     * @param owner    the view's owner
     * @param observer the which observes changes
     */
    public void addRequestListObserver(@NonNull LifecycleOwner owner, @NonNull Observer<? super List<Connection>> observer) {
        mRequestsList.observeForever(observer);
    }

    /**
     * A method to add an observer to the list of user sent requests
     *
     * @param owner    the view's owner
     * @param observer the which observes changes
     */
    public void addSentRequestsListObserver(@NonNull LifecycleOwner owner, @NonNull Observer<? super List<Connection>> observer) {
        mSentRequestsList.observeForever(observer);
    }

    /**
     * A method to add an observer to the userID
     *
     * @param owner    the view's owner
     * @param observer the which observes changes
     */
    public void addUserIDObserver(@NonNull LifecycleOwner owner, Observer<? super  Integer> observer){
        userID.observe(owner,observer);
    }

    /**
     * A method to add an observer to the second userID, used in the case of searching for connections
     *
     * @param owner    the view's owner
     * @param observer the which observes changes
     */
    public void addSecondIDObserver(@NonNull LifecycleOwner owner, Observer<? super Integer> observer){
        secondID.observe(owner,observer);
    }

    /**
     * A method to add an observer to the search list
     *
     * @param owner    the view's owner
     * @param observer the which observes changes
     */
    public void addSearchListObserver(@NonNull LifecycleOwner owner, @NonNull Observer<? super List<Connection> >observer){
        mSearchList.observe(owner,observer);
    }

    /**
     * A method to add an observer to the error response
     *
     * @param owner    the view's owner
     * @param observer the which observes changes
     */
    public void addResponseObserver(@NonNull LifecycleOwner owner, @NonNull Observer<? super String> observer) {
        mResponse.observe(owner, observer);
    }





    /**
     * A method to get the userID from the server
     *
     * @param email the email of the user who's id we want
     * */
    public void getUserID(final String email) {
        String url = infoUrl;

        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                this::handleID,
                this::handleError
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("email", email);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }

    /**
     * A method to get the userID from the server
     *
     * @param email the email of the user who's id we want
     */
    public void getSecondID(final String email){

        String url = infoUrl;
        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                this::handleSecondID,
                this::handleError)
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("email", email);
                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }
    /**
     * A method to get user info from the web service based on a query string
     *
     * @param query part of the nickname to search for
     * */
    public void searchConnections(final String query){
        String url = connectionRequestsUrl + "/search";
        list = Lists.SEARCH;
        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                this::handleResult, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();


                // add headers <key,value>
                headers.put("Authorization", "adfs");
                headers.put("query", query);

                return headers;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }

    /**
     * A method to get the user connections
     *
     * @param userID the userID of the user we want the connections for
     */
    public void getConnections(final int userID) {

        String url = connectionsUrl;
        list = Lists.CONNECTIONS;
        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                this::handleResult, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                // add headers <key,value>
                headers.put("Authorization", "adfs");
                headers.put("id1", "" + userID + "");

                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }

    /**
     * A method to get the incoming requests for a user
     *
     * @param userID the userID of the user's requests that we want
     * */
    public void getIncomingRequests(final int userID) {
        String url = connectionRequestsUrl + "/incoming";
        list = Lists.REQUESTS;
        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                this::handleResult, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();


                // add headers <key,value>
                headers.put("Authorization", "adfs");
                headers.put("id", "" + userID + "");

                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }

    /**
     * A method to get all of the outgoing requests for a user
     *
     * @param userID the userID of the user who's outgoing requests we want
     * */
    public void getOutgoingRequests(int userID) {
        String url = connectionRequestsUrl;
        list = Lists.SENT;
        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                this::handleResult, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                // add headers <key,value>
                headers.put("Authorization", "adfs");
                headers.put("id1", "" + userID + "");

                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }

    /**
     * A method to send a request to a user
     *
     * @param userID the id of the user sending the request
     * @param targetID the id of the receiver of the request
     * */
    public void sendRequest(int userID, int targetID) {
        String url = connectionRequestsUrl;
        list = Lists.SENT;
        JSONObject body = new JSONObject();
        try{

            body.put("id1", userID);
            body.put("id2", targetID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Request request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                this::handleResult,
                this::handleError
        );
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }

    /**
     * A method to remove a connection from a user
     *
     * @param userID the id of the user removing the connection
     * @param targetID the id of the connection to be removed
     * */
    public void removeConnection(int userID, int targetID) {
        String url = connectionsUrl;
        list = Lists.CONNECTIONS;
        Request request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                this::handleResult, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                // add headers <key,value>
                headers.put("Authorization", "adfs");
                headers.put("id1", "" + userID + "");
                headers.put("id2", "" + targetID);

                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }

    /**
     * A method to accept a request from a user
     *
     * @param userID the id of the user accepting the request
     * @param targetID the id of the user who sent the request
     * */
    public void acceptRequest(int userID, int targetID) {
        String url = connectionsUrl;
        list = Lists.CONNECTIONS;
        JSONObject body = new JSONObject();
        try {

            body.put("id1", userID);
            body.put("id2", targetID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Request request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                this::handleResult,
                this::handleError
        );
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }
    /**
     * A method to decline a request
     *
     * @param userID the id of the user removing/canceling the request
     * @param targetID the id of the other user attached to the request
     * */
    public void declineRequest(int userID, int targetID, String location) {

        String url = connectionRequestsUrl;
        if(location.equals("Sent")){
            list = Lists.SENT;
        }
        else {
            list = Lists.REQUESTS;
        }
        Request request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                this::handleResult, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                // add headers <key,value>
                headers.put("Authorization", "adfs");
                headers.put("id1", "" + userID + "");
                headers.put("id2", ""+targetID);

                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }

    /**
     * A method to handle the errors on the requests
     *
     * @param error the error data returned from the server
     * */
    private void handleError(final VolleyError error) {
        Log.e("CONNECTION ERROR", "Connections");
    }

    /**
     * A method to handle the result of most of the requests
     *
     * @param result the jsonObject which contains the response
     * */
    private void handleResult(final JSONObject result) {

        IntFunction<String> getString = getApplication().getResources()::getString;
        try {
            JSONObject root = result;

            //Check if there are rows to set
            if (root.has(getString.apply(R.string.keys_json_blogs_response))) {
                //Check if rows is empty
                if (root.has(getString.apply(R.string.keys_json_blogs_response)) && root.has("rowCount")) {
                    JSONArray data = root.getJSONArray(getString.apply(R.string.keys_json_blogs_response));
                    //Empty all of the lists
                    mSearchList.setValue(new ArrayList<>());
                    mConnectionsList.setValue(new ArrayList<>());
                    mRequestsList.setValue(new ArrayList<>());
                    mSentRequestsList.setValue(new ArrayList<>());
                    //Loop over the response data
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject jsonConnection = data.getJSONObject(i);
                        //For each jsonConnection we need to grab the name and info with another request

                        Connection connection = new Connection(jsonConnection.getString(getString.apply(R.string.keys_json_connections_fname)) + " " + jsonConnection.getString(getString.apply(R.string.keys_json_connections_lname)),
                                jsonConnection.getString(getString.apply(R.string.keys_json_connections_email)),
                                jsonConnection.getInt(getString.apply(R.string.keys_json_connections_id)),
                                jsonConnection.getString("nickname"));

                        //Put the connections in the correct list
                        switch (list) {
                            case SENT:

                                if (!mSentRequestsList.getValue().contains(connection)) {
                                    mSentRequestsList.getValue().add(connection);
                                }
                                break;
                            case REQUESTS:
                                if (!mRequestsList.getValue().contains(connection)) {
                                    mRequestsList.getValue().add(connection);
                                }
                                break;
                            case CONNECTIONS:

                                if (!mConnectionsList.getValue().contains(connection)) {
                                    mConnectionsList.getValue().add(connection);
                                }
                                break;
                            case SEARCH:
                                if(!mSearchList.getValue().contains(connection)){
                                    mSearchList.getValue().add(connection);
                                }
                                break;
                            default:
                                break;
                        }
                    }

                    //If there are no values set the list to itself
                    switch (list) {
                        case SENT:
                            mSentRequestsList.setValue(mSentRequestsList.getValue());
                            break;
                        case REQUESTS:
                            mRequestsList.setValue(mRequestsList.getValue());
                            break;
                        case CONNECTIONS:
                            mConnectionsList.setValue(mConnectionsList.getValue());
                            break;
                        case SEARCH:
                            mSearchList.setValue(mSearchList.getValue());
                            break;
                        default:
                            break;
                    }

                } else {

                    //This happens when this lists are empty so empty the lists
                    mSearchList.setValue(new ArrayList<>());
                    mConnectionsList.setValue(new ArrayList<>());
                    mRequestsList.setValue(new ArrayList<>());
                    mSentRequestsList.setValue(new ArrayList<>());
                }
            }
            else if(root.has("success")){
                if(root.getBoolean("success") == true){
                    //Log the new list for posterity
                    switch (list) {
                        case SENT:
                            Log.d("Sent", mSentRequestsList.getValue().toString());
                            //getOutgoingRequests(this.userID.getValue());
                            break;
                        case REQUESTS:
                            Log.d("Requests", mRequestsList.getValue().toString());
                            //getIncomingRequests(this.userID.getValue());
                            break;
                        case CONNECTIONS:
                            Log.d("Connections", mConnectionsList.getValue().toString());
                           // getConnections(this.userID.getValue());
                            break;
                        default:
                            break;
                    }
                }
            }

            else {
                Log.e("ERROR!", root.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }
    }

    /**
     * A method to handle the result of an id query
     *
     * @param result the jsonObject that contains the response
     * */
    private void handleID(JSONObject result) {

        IntFunction<String> getString = getApplication().getResources()::getString;
        try {
            JSONObject root = result;
            if (root.has(getString.apply(R.string.keys_json_blogs_response))) {
                JSONArray data = root.getJSONArray(getString.apply(R.string.keys_json_blogs_response));

                for (int i = 0; i < data.length(); i++) {
                    JSONObject id = data.getJSONObject(i);
                    userID.setValue(id.getInt(getString.apply(R.string.keys_json_connections_id)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * A method to handle the result of an id query
     *
     * @param result the jsonObject that contains the response
     */
    private void handleSecondID(JSONObject result) {

        IntFunction<String> getString = getApplication().getResources()::getString;
        try {
            JSONObject root = result;

            if (root.has(getString.apply(R.string.keys_json_blogs_response))) {
                JSONArray data = root.getJSONArray(getString.apply(R.string.keys_json_blogs_response));

                for (int i = 0; i < data.length(); i++) {
                    JSONObject id = data.getJSONObject(i);
                    secondID.setValue(id.getInt(getString.apply(R.string.keys_json_connections_id)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
