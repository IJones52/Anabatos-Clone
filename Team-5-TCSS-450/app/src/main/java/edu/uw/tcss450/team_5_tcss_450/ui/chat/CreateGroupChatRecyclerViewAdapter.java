package edu.uw.tcss450.team_5_tcss_450.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentCreateChatCardBinding;
import edu.uw.tcss450.team_5_tcss_450.ui.connections.Connection;

/**
 * The adapter for the dialog that allows the user to create a group chat.
 *
 * @author Khue Nguyen
 * @version 6/5/2021
 */
public class CreateGroupChatRecyclerViewAdapter extends
        RecyclerView.Adapter<CreateGroupChatRecyclerViewAdapter.GroupConnectionViewHolder> {

    /**
     * A listening interface that listens for buttons that are selected.
     */
    interface OnUserSelectedListener {
        void onUserSelected(Connection connection);
        void onUserUnselected(Connection connection);
    }

    /**
     * List of connections
     */
    private final List<Connection> mConnections;

    /**
     * Keeps track of the selected buttons.
     */
    private final Map<Connection, Boolean> mSelectedFlags;

    /**
     * An instance of a listener that listens for buttons that are selected.
     */
    @NonNull
    private CreateGroupChatRecyclerViewAdapter.OnUserSelectedListener onUserClick;

    /**
     * Creates an instance of the CreateGroupChatRecyclerViewAdapter object.
     * @param connections list of connection
     * @param onUserSelectedListener listener that listens for buttons that are selected
     */
    public CreateGroupChatRecyclerViewAdapter(List<Connection> connections, @NonNull CreateGroupChatRecyclerViewAdapter.OnUserSelectedListener onUserSelectedListener) {
        mConnections = connections;
        onUserClick = onUserSelectedListener;
        mSelectedFlags = mConnections.stream().collect(Collectors.toMap(Function.identity(), connection -> false));
    }

    @NonNull
    @Override
    public GroupConnectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupConnectionViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_create_chat_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupConnectionViewHolder holder, int position) {
        holder.setCard(mConnections.get(position));
    }

    @Override
    public int getItemCount() {
        return mConnections.size();
    }

    /**
     * An inner class that represents the individual views of each list item within the
     * Recyclerview. Each list items represent a connection.
     */
    public class GroupConnectionViewHolder extends RecyclerView.ViewHolder {
        /**
         * The text view of the connection's name.
         */
        private final TextView mName;

        /**
         * The button that indicates the user's selection to this connnection.
         */
        private final RadioButton mSelect;

        /**
         * Instantiates the GroupConnectionViewHolder object.
         * @param itemView the view
         */
        public GroupConnectionViewHolder(@NonNull View itemView) {
            super(itemView);
            FragmentCreateChatCardBinding binding = FragmentCreateChatCardBinding.bind(itemView);
            mName = binding.textName;
            mSelect = binding.buttonSelect;
        }

        /**
         * A method that sets the view with the corresponding information of the given connection.
         *
         * @param connection the connection
         */
        void setCard(Connection connection) {
            mName.setText(connection.getName());
            mSelect.setChecked(mSelectedFlags.get(connection));
            mSelect.setSelected(mSelectedFlags.get(connection));
            if (mSelectedFlags.get(connection)) {
                onUserClick.onUserSelected(connection);
            } else {
                onUserClick.onUserUnselected(connection);
            }

            mSelect.setOnClickListener(button -> {
                mSelectedFlags.put(connection, !mSelectedFlags.get(connection));
                notifyDataSetChanged();
            });
        }


    }
}
