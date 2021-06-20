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
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentSentRequestsCardBinding;
import edu.uw.tcss450.team_5_tcss_450.model.UserInfoViewModel;

/**
 * The RecyclerViewAdapter for the list of sent connection requests
 * @author Ismael Jones
 * @version 5/4/21
 * */
public class SentRequestsRecyclerViewAdapter extends RecyclerView.Adapter<SentRequestsRecyclerViewAdapter.SentRequestViewHolder> {
    //The list of connections which requests were sent to
    private List<Connection> mConnections;

    /**
     * Initialize a new SentRequestsRecyclerViewAdapter
     * @param items the list of requests for the user
     */
    public SentRequestsRecyclerViewAdapter(List<Connection> items) {
        this.mConnections = items;
    }

    @NonNull
    @Override
    public SentRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SentRequestViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.fragment_sent_requests_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SentRequestsRecyclerViewAdapter.SentRequestViewHolder holder, int position) {

        holder.setConnection(mConnections.get(position));
    }

    @Override
    public int getItemCount() {
        //Just a temp to show a few examples
        return mConnections.size();
    }
    /**
     * The inner class which handles creating each card for each sent request
     * */
    public class SentRequestViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public FragmentSentRequestsCardBinding mBinding;
        public Connection mConnection;

        /**
         * Initialize a new card for a sent request
         * @param view the card to initalize
         * */
        public SentRequestViewHolder(@NonNull View view) {
            super(view);
            mView = view;
            mBinding = FragmentSentRequestsCardBinding.bind(view);


        }

        /**
         * Set the connection information for the specific card
         * @param connection the connection's information to populate the card
         * */
        private void setConnection(Connection connection) {
            mBinding.buttonCancel.setOnClickListener(this::cancelRequest);
            mBinding.labelName.setText(connection.getName());
            mConnection = connection;
            mBinding.labelAt.setText("@" + mConnection.getNickname());

        }


        /**
         * Cancels a pending request from the user after confirmation
         * @param view the card and related connection to cancel the request to
         * */
        private void cancelRequest(View view) {
            Log.d("Connections", "Cancel Request");
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

            builder.setTitle(R.string.action_confrimation);
            builder.setPositiveButton(R.string.action_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Do nothing
                }
            });
            builder.setNegativeButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Set up the listener before declining a request
                    ConnectionsViewModel mModel = new ViewModelProvider((ViewModelStoreOwner) view.getContext()).get(ConnectionsViewModel.class);
                    UserInfoViewModel userModel = new ViewModelProvider((ViewModelStoreOwner) view.getContext()).get(UserInfoViewModel.class);
                    mModel.declineRequest(userModel.getmId(), mConnection.getId(), "Sent");
                    mConnections.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());

                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
