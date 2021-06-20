package edu.uw.tcss450.team_5_tcss_450.ui.connections;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.List;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentConnectionsCardBinding;
import edu.uw.tcss450.team_5_tcss_450.model.UserInfoViewModel;
import edu.uw.tcss450.team_5_tcss_450.ui.chat.ChatListViewModel;
import edu.uw.tcss450.team_5_tcss_450.ui.chat.ChatRoomInfo;
import edu.uw.tcss450.team_5_tcss_450.ui.chat.ChatViewModel;

/**
 * The RecyclerViewAdapter for the ConnectionsFragment. Very similar to the other two adapters but
 * focuses on connections
 *
 * @author Ismael Jones
 * @version 5/19/21
 */
public class ConnectionsRecyclerViewAdapter extends
        RecyclerView.Adapter<ConnectionsRecyclerViewAdapter.ConnectionsViewHolder> {
    //The list of the users connections
    private final List<Connection> mConnections;

    /**
     * Initialize a new ConnectionsRecyclerViewAdapter
     *
     * @param items the list of requests for the user
     */
    public ConnectionsRecyclerViewAdapter(List<Connection> items) {
        this.mConnections = items;
    }

    @NonNull
    @Override
    public ConnectionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConnectionsViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.fragment_connections_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectionsViewHolder holder, int position) {
        //Set to a premade connection because the list is empty
        holder.setConnection(mConnections.get(position));
    }

    @Override
    public int getItemCount() {

        return mConnections.size();
    }

    /**
     * An inner class to handle each specific connection in the list
     */
    public class ConnectionsViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public FragmentConnectionsCardBinding mBinding;
        public Connection mConnection;


        /**
         * A method to instantiate the ConnectionsRequestViewHolder
         *
         * @param view the specific card that is being instantiated
         */
        public ConnectionsViewHolder(@NonNull View view) {
            super(view);
            mView = view;
            mBinding = FragmentConnectionsCardBinding.bind(view);


        }

        /**
         * A method to populate the card with the specific information of a connection
         *
         * @param connection the connection whose information will go in the card
         */
        private void setConnection(Connection connection) {
            mConnection = connection;
            mBinding.buttonChat.setOnClickListener(view -> {
                ChatListViewModel chatList  = new ViewModelProvider((ViewModelStoreOwner) view.getContext()).get(ChatListViewModel.class);
                UserInfoViewModel userModel = new ViewModelProvider((ViewModelStoreOwner) view.getContext()).get(UserInfoViewModel.class);
                try{
                    chatList.addChatListObserver((LifecycleOwner) view.getContext(), chatRooms -> {
                       boolean found = false;
                        //See if the chat room exists, if it does send them to it
                        for (ChatRoomInfo chat : chatRooms) {
                            Log.d(chat.getmChatName(), chat.getmChatMembers().size() + "");
                            if (chat.getmChatMembers().size() == 1) {
                                if (chat.getmChatMembers().get(0).equals(connection)) {
                                    found = true;
                                    Navigation.findNavController(mView).navigate(
                                            ConnectionsFragmentDirections.actionNavigationContactsToChatRoomFragment(chat)
                                    );
                                }
                            }
                        }
                        if(!found){
                            Navigation.findNavController(mView).navigate(
                                    ConnectionsFragmentDirections.actionNavigationContactsToCreateIndivChatFragment()
                            );
                        }


                    });
                }
                catch (NullPointerException e){
                   chatList.init(userModel.getmJwt());
                }

            });
            mBinding.buttonRemove.setOnClickListener(this::removeConnection);
            mBinding.labelName.setText(connection.getName());

            mBinding.labelAt.setText("@"  + mConnection.getNickname());
        }

        /**
         * A method to remove a connection from the list upon a user's confirmation
         *
         * @param view the card and corresponding connection to delete
         */
        private void removeConnection(View view) {
            Log.d("Connections", "Remove Connection");
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

            builder.setTitle(R.string.action_confrimation);
            builder.setPositiveButton(R.string.action_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Do nothing
                }
            });
            builder.setNegativeButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Set up the listener to handle deleting requests
                    ConnectionsViewModel mModel = new ViewModelProvider((ViewModelStoreOwner) view.getContext()).get(ConnectionsViewModel.class);
                    UserInfoViewModel userModel = new ViewModelProvider((ViewModelStoreOwner) view.getContext()).get(UserInfoViewModel.class);
                    mModel.removeConnection( mConnection.getId(), userModel.getmId());
                    mConnections.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());


                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
