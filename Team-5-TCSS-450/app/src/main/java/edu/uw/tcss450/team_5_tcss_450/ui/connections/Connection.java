package edu.uw.tcss450.team_5_tcss_450.ui.connections;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;

/**
 * A helper class that holds the information for an individual connection
 * @author Ismael Jones
 * @version 5/19/21
 * */
public class Connection implements Serializable {

    private  String mName;
    private  String mEmail;
    private int mId;
    private String mNickname;

    /**
     * Create a Connection
     * For now just contains an email and a name
     * @param name the user's name
     * @param email the user's email
     * @param id the user's id
     * @param nickname the user's nickname
     * */
    public Connection(String name, String email, int id, String nickname){
        mName = name;
        mEmail = email;
        mId = id;
        //A lot of the accounts were created before nicknames were implemented so we have a placeholder

        if(!nickname.equals("null")){
            mNickname = nickname;
        }
        else{
            mNickname = "null";
        }
    }

    /**
     * A method to convert a json string into a connection object
     * @param connectionAsJson the string representation of the jsonobject containing the connection
     * */
    public static Connection createFromJsonString(final String connectionAsJson) throws JSONException {
        final JSONObject conn = new JSONObject(connectionAsJson);
        final String name = conn.getString("firstname") + " " + conn.getString("lastname");
        final String nickname = conn.getString("nickname");
        return new Connection(name, conn.getString("email"), conn.getInt("memberid"), nickname);
    }

    /**
     * Getters
     * @return the private value specified in the method name
     * */
    public String getEmail() {
        return mEmail;
    }

    public String getName() {
        return mName;
    }

    public int getId(){return mId;}

    public String getNickname(){return mNickname;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return mId == that.mId &&
                Objects.equals(mName, that.mName) &&
                Objects.equals(mEmail, that.mEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mName, mEmail, mId);
    }

    @Override
    public String toString() {
        return "Connection{" +
                "mName='" + mName + '\'' +
                ", mId=" + mId +
                '}';
    }
}