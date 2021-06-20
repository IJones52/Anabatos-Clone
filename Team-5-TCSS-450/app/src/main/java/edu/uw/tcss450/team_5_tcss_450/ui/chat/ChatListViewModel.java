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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.io.RequestQueueSingleton;
import edu.uw.tcss450.team_5_tcss_450.ui.connections.Connection;

/**
 * The View Model for a list of chats.
 *
 * @author Khue Nguyen
 * @version 5/19/2021
 */
public class ChatListViewModel extends AndroidViewModel {
    /**
     * The MutableLiveData object for list of chats.
     */
    private MutableLiveData<List<ChatRoomInfo>> mChats;

    /**
     * A constructor that creates a new instance of the ChatListViewModel
     * @param application the application
     */
    public ChatListViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Observes the chat list for any changes.
     * @param owner the lifecycle owner
     * @param observer the observer
     */
    public void addChatListObserver(@NonNull LifecycleOwner owner, @NonNull Observer<? super List<ChatRoomInfo>> observer) {
        mChats.observe(owner, observer);
    }

    /**
     * Get initial values for the chat rooms.
     * @param jwt the user's jwt
     */
    public void init(String jwt) {
        mChats = new MutableLiveData<>();
        mChats.setValue(new ArrayList<>());

        String url = getApplication().getResources().getString(R.string.base_url) + "chats/";
        Request request = new JsonObjectRequest(Request.Method.GET, url, null, this::handleGetInit,
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

    public ChatRoomInfo getChatRoomInfoGivenMembers(List<Connection> list) {
        ChatRoomInfo result = null;
        for (ChatRoomInfo chatRoomInfo : mChats.getValue()) {
            List<Connection> members = chatRoomInfo.getmChatMembers();
            if (new HashSet<>(members).equals(new HashSet<>(list))) {
                result = chatRoomInfo;
            }
        }

        return result;
    }

    /**
     * Request to create a chat room with an individual.
     * @param member member to be added.
     * @param jwt the jwt of the user.
     */
    public void createIndivChatRoom(List<Connection> member, String jwt) {
        if (member.size() > 1) {
            throw new IllegalArgumentException("List of connections must only have one member");
        }
        String name = "Individual Chat";
        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            JSONArray memberArray = new JSONArray();
            memberArray.put(member.get(0).getId());
            body.put("members", memberArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = getApplication().getResources().getString(R.string.base_url) + "chats";
        Request request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    try {
                        int chatid = response.getInt("chatid");
                        ChatRoomInfo chatRoomInfo = new ChatRoomInfo.Builder(chatid, name)
                                .addMember(member.get(0)).build();
                        mChats.getValue()
                                .add(0, chatRoomInfo); // newly created chats are most recent
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mChats.setValue(mChats.getValue());
                },
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
     * Request to create a chat room with a group.
     * @param name name of the chat
     * @param members list of members to add
     * @param jwt the jwt of the user
     */
    public void createGroupChatRoom(String name, List<Connection> members, String jwt) {
        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            JSONArray membersArray = new JSONArray();
            for (Connection c : members) {
                membersArray.put(c.getId());
            }
            body.put("members", membersArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = getApplication().getResources().getString(R.string.base_url) + "chats";
        Request request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    try {
                        int chatid = response.getInt("chatid");
                        ChatRoomInfo chatRoomInfo = new ChatRoomInfo.Builder(chatid, name)
                                .addMembers(members).build();
                        mChats.getValue()
                                .add(0, chatRoomInfo); // newly created chats are most recent
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mChats.setValue(mChats.getValue());
                },
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
     * Request to delete the chat room.
     * @param chatRoomInfo the chat to be deleted.
     * @param jwt the user's jwt.
     */
    public void deleteChat(ChatRoomInfo chatRoomInfo, String jwt) {
        String url = getApplication().getResources()
                .getString(R.string.base_url) + "chats/" + chatRoomInfo.getmChatId();
        Request request = new JsonObjectRequest(Request.Method.DELETE, url, null, response -> {
            mChats.getValue().remove(chatRoomInfo);
            mChats.setValue(mChats.getValue());
        }, this::handleError);
        request.setRetryPolicy(
                new DefaultRetryPolicy(10_000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }

    /**
     * Request the user to leave the chat.
     * @param chatRoomInfo the chat
     * @param memberId the member id of the user that's leaving
     * @param jwt the user's jwt
     */
    public void leaveChat(ChatRoomInfo chatRoomInfo, int memberId, String jwt) {
        String url = getApplication().getResources()
                .getString(R.string.base_url) + "chats/" + chatRoomInfo
                .getmChatId() + "/" + memberId;
        Request request = new JsonObjectRequest(Request.Method.DELETE, url, null, response -> {
            mChats.getValue().remove(chatRoomInfo);
            mChats.setValue(mChats.getValue());
        }, this::handleError);
        request.setRetryPolicy(
                new DefaultRetryPolicy(10_000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueueSingleton.getInstance(getApplication().getApplicationContext())
                .addToRequestQueue(request);
    }

    /**
     * Update recent message of a chatroom given its chat id.
     * @param id the chat id
     * @param message the recent message to be updated
     */
    public void updateRecentMessage(int id, ChatMessage message) {
        for (int i = 0; i < mChats.getValue().size(); i++) {
            ChatRoomInfo oldInfo = mChats.getValue().get(i);
            if (oldInfo.getmChatId() == id) {
                ChatRoomInfo newInfo = new ChatRoomInfo.Builder(oldInfo.getmChatId(),
                        oldInfo.getmChatName()).addMembers(oldInfo.getmChatMembers())
                        .addRecentMsg(message).build();
                mChats.getValue().set(i, newInfo);
                break;
            }
        }

        mChats.setValue(ChatRoomInfo.sortList(mChats.getValue()));
    }

    /**
     * A helper method that handles a successful init request. Parses the response and updates the
     * value of mChats.
     *
     * @param response
     */
    private void handleGetInit(JSONObject response) {
        if (!response.has("rows")) {
            throw new IllegalStateException(
                    "Unexpected response in ChatListViewModel (no rows): " + response);
        }

        try {
            JSONArray values = response.getJSONArray("rows");
            for (int i = 0; i < values.length(); i++) {
                JSONObject jsonInfo = values.getJSONObject(i);
                int id = jsonInfo.getInt("chatid");
                String chatName = jsonInfo.getString("name");
                ChatRoomInfo chatRoomInfo = new ChatRoomInfo.Builder(id, chatName).build();

                if (jsonInfo.has("members")) {
                    JSONArray jsonMembers = jsonInfo.getJSONArray("members");
                    List<Connection> members = new ArrayList<>();
                    for (int j = 0; j < jsonMembers.length(); j++) {
                        JSONObject jsonMember = jsonMembers.getJSONObject(j);
                        String name = jsonMember.getString("firstname") + " " + jsonMember
                                .getString("lastname");
                        String email = jsonMember.getString("email");
                        int memberId = jsonMember.getInt("memberid");
                        String nickname = "";
                        if (jsonMember.has("nickname") && !jsonMember.isNull("nickname"))
                            nickname = jsonMember.getString("nickname");
                        Connection member = new Connection(name, email, memberId, nickname);
                        members.add(member);
                    }
                    chatRoomInfo = new ChatRoomInfo.Builder(id, chatName).addMembers(members)
                            .build();

                    if (jsonInfo.has("message")) {
                        JSONObject jsonMsg = jsonInfo.getJSONObject("message");
                        int messageId = jsonMsg.getInt("messageid");
                        String name = jsonMsg.getString("firstname") + " " + jsonMsg
                                .getString("lastname");
                        String email = jsonMsg.getString("email");
                        String text = jsonMsg.getString("message");
                        String timestamp = jsonMsg.getString("timestamp");
                        ChatMessage msg = new ChatMessage(messageId, text, name, email, timestamp);
                        chatRoomInfo = new ChatRoomInfo.Builder(id, chatName).addMembers(members)
                                .addRecentMsg(msg).build();
                    }
                }

                mChats.getValue().add(chatRoomInfo);
            }
        } catch (JSONException e) {
            Log.e("JSON Parse Error", "Found in ChatListVM.handleGetInit: " + e.getMessage());
        }

        mChats.setValue(ChatRoomInfo.sortList(mChats.getValue()));
    }

    /**
     * A helper method that handles an error in requests.
     *
     * @param error the error to be handled.
     */
    private void handleError(VolleyError error) {
        if (Objects.isNull(error.networkResponse)) {
            Log.e("CHATLIST NETWORK ERROR", error.getMessage());
        } else {
            String data = new String(error.networkResponse.data, Charset.defaultCharset());
            Log.e("CHATLIST CLIENT ERROR",
                    error.networkResponse.statusCode +
                            " " +
                            data);
        }
    }
}
