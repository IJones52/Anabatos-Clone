package edu.uw.tcss450.team_5_tcss_450.ui.connections;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentConnectionRequestCardBinding;
import edu.uw.tcss450.team_5_tcss_450.model.UserInfoViewModel;
import edu.uw.tcss450.team_5_tcss_450.notifications.NotificationListViewModel;

/**
 * The RecyclerViewAdapter for the ConnectionRequestFragment. Very similar to the other two adapters but focuses on requests
 * @author Ismael Jones
 * @version 5/19/21
 * */

public class ConnectionRequestRecyclerViewAdapter extends RecyclerView.Adapter<ConnectionRequestRecyclerViewAdapter.ConnectionsRequestViewHolder> {
    //The list of connections to be used as requests
    private List<Connection> mConnections;


    /**
     * Initialize a new ConnectionRequestRecyclerViewAdapter
     * @param items the list of requests for the user
     * */
    public ConnectionRequestRecyclerViewAdapter(List<Connection> items) {
        this.mConnections = items;
    }

    @NonNull
    @Override
    public ConnectionsRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConnectionsRequestViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.fragment_connection_request_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectionRequestRecyclerViewAdapter.ConnectionsRequestViewHolder holder, int position) {

        holder.setConnection(mConnections.get(position));
    }

    @Override
    public int getItemCount() {
        return mConnections.size();
    }

    /**
     * The inner class to handle each specific connection request
     * */
    public class ConnectionsRequestViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public FragmentConnectionRequestCardBinding mBinding;
        public Connection mConnection;

        /**
         * A method to instantiate the ConnectionsRequestViewHolder
         * @param view the specific card that is being instantiated
         * */
        public ConnectionsRequestViewHolder(@NonNull View view) {
            super(view);
            mView = view;
            mBinding = FragmentConnectionRequestCardBinding.bind(view);

        }


        /**
         * A method that populates the card in the RecyclerView
         * @param connection the connection object with information on the connection
         * */
        private void setConnection(Connection connection) {
            mBinding.buttonAccept.setOnClickListener(this::acceptRequest);
            mBinding.buttonDecline.setOnClickListener(this::declineRequest);
            mBinding.labelName.setText(connection.getName());
            mConnection = connection;
            mBinding.labelAt.setText("@" + mConnection.getNickname());

        }
        /**
         * A method that will handle when a user accepts a request
         * @param view the specific card and by extension connection which has been accepted
         * */
        private void acceptRequest(View view) {
            Log.d("Connections", "Accept Connection");
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

            builder.setTitle(R.string.action_confrimation);
            builder.setPositiveButton(R.string.action_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Do nothing
                }
            });
            builder.setNegativeButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Accept the request
                    ConnectionsViewModel mModel = new ViewModelProvider((ViewModelStoreOwner) view.getContext()).get(ConnectionsViewModel.class);
                    UserInfoViewModel userModel = new ViewModelProvider((ViewModelStoreOwner) view.getContext()).get(UserInfoViewModel.class);
                    mModel.acceptRequest(mConnection.getId(), userModel.getmId());
                    mConnections.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());

                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        /**
         * A method that will handle when a user declines a request
         * @param view the specific card and by extension connection which has been declined
         */
        private void declineRequest(View view) {
            Log.d("Connections", "Decline Connection");
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

            builder.setTitle(R.string.action_confrimation);
            builder.setPositiveButton(R.string.action_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Do nothing
                }
            });
            builder.setNegativeButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Set up the listener for the userid before declining the requests
                    ConnectionsViewModel mModel = new ViewModelProvider((ViewModelStoreOwner) view.getContext()).get(ConnectionsViewModel.class);
                    UserInfoViewModel userModel = new ViewModelProvider((ViewModelStoreOwner) view.getContext()).get(UserInfoViewModel.class);
                    mModel.declineRequest(mConnection.getId(), userModel.getmId(), "");
                    mConnections.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());

                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
