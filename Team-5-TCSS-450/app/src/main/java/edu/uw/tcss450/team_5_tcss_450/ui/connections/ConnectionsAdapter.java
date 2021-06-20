package edu.uw.tcss450.team_5_tcss_450.ui.connections;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import edu.uw.tcss450.team_5_tcss_450.R;

public class ConnectionsAdapter extends ArrayAdapter<Connection> {


    public ConnectionsAdapter(@NonNull Context context, int resource, @NonNull List<Connection> objects) {
        super(context, 0, objects);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Connection connection = getItem(position);

        //Set up the dropdown for the searchbar
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_connection, parent, false);
        TextView userName = (TextView) convertView.findViewById(R.id.label_user);
        TextView nickname = (TextView) convertView.findViewById(R.id.label_nickname);
        userName.setText(connection.getName());
        nickname.setText("@" + connection.getNickname());

        return convertView;
    }
}
