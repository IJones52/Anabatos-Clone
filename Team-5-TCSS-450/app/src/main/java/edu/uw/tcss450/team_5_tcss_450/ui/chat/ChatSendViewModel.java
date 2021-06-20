package edu.uw.tcss450.team_5_tcss_450.ui.chat;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.io.RequestQueueSingleton;

/**
 * Keeps track of the response of the message being sent.
 * @author Khue Nguyen
 * @version 6/5/2021
 */
public class ChatSendViewModel extends AndroidViewModel {
    /**
     * The request's response.
     */
    private final MutableLiveData<JSONObject> mResponse;

    /**
     * A constructor that creates a new instance of the ChatSendViewModel
     * @param application the application
     */
    public ChatSendViewModel(@NonNull Application application) {
        super(application);
        mResponse = new MutableLiveData<>();
        mResponse.setValue(new JSONObject());
    }

    /**
     * Observes changes to the response.
     * @param owner the lifecycle owner
     * @param observer the observer
     */
    public void addResponseObserver(@NonNull LifecycleOwner owner, @NonNull Observer<? super JSONObject> observer) {
        mResponse.observe(owner, observer);
    }

    /**
     * Requests to send a message.
     * @param chatId the chat room's id
     * @param jwt the user's jwt
     * @param message the message being sent
     */
    public void sendMessage(final int chatId, final String jwt, final String message) {
        String url = getApplication().getResources()
                .getString(R.string.base_url) + "messages";
        JSONObject body = new JSONObject();
        try {
            body.put("message", message);
            body.put("chatId", chatId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request request = new JsonObjectRequest(Request.Method.POST, url, body, mResponse::setValue,
                this::handleError) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", jwt);
                return headers;
            }
        };

        request.setRetryPolicy(
                new DefaultRetryPolicy(10_000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }

    /**
     * A helper method that handles an error in requests.
     *
     * @param error the error to be handled.
     */
    private void handleError(final VolleyError error) {
        if (Objects.isNull(error.networkResponse)) {
            Log.e("NETWORK ERROR", error.getMessage());
        } else {
            String data = new String(error.networkResponse.data, Charset.defaultCharset());
            Log.e("CLIENT ERROR", error.networkResponse.statusCode + " " + data);
        }
    }
}
