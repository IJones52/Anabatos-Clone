package edu.uw.tcss450.team_5_tcss_450.ui.chat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentCreateChatCardBinding;
import edu.uw.tcss450.team_5_tcss_450.ui.connections.Connection;

/**
 * The adapter for the dialog that allows the user to start a chat with one of their connections.
 *
 * @author Khue Nguyen
 * @version 6/5/2021
 */
public class CreateIndivChatRecyclerViewAdapter extends
        RecyclerView.Adapter<CreateIndivChatRecyclerViewAdapter.IndivConnectionViewHolder> {

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
    private List<Connection> mConnections;

    /**
     * Keeps track of the last checked position of the item in the list.
     */
    private int mLastCheckedPos = -1;

    /**
     * An instance of a listener that listens for buttons that are selected.
     */
    @NonNull
    private OnUserSelectedListener onUserClick;

    /**
     * Creates an instance of the CreateIndivChatRecyclerViewAdapter object.
     * @param connections list of connection
     * @param onUserSelectedListener listener that listens for buttons that are selected
     */
    public CreateIndivChatRecyclerViewAdapter(List<Connection> connections, @NonNull OnUserSelectedListener onUserSelectedListener) {
        mConnections = connections;
        onUserClick = onUserSelectedListener;
    }

    @NonNull
    @Override
    public IndivConnectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new IndivConnectionViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_create_chat_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull IndivConnectionViewHolder holder, int position) {
        holder.mName.setText(mConnections.get(position).getName());
        holder.mSelect.setChecked(position == mLastCheckedPos);
        if (position == mLastCheckedPos) {
            onUserClick.onUserSelected(mConnections.get(position));
        } else {
            onUserClick.onUserUnselected(mConnections.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mConnections.size();
    }

    /**
     * An inner class that represents the individual views of each list item within the
     * Recyclerview. Each list items represent a connection.
     */
    public class IndivConnectionViewHolder extends RecyclerView.ViewHolder {
        /**
         * The text view of the connection's name.
         */
        private final TextView mName;

        /**
         * The button that indicates the user's selection to this connnection.
         */
        private final RadioButton mSelect;

        /**
         * Instantiates the IndivConnectionViewHolder object.
         * @param itemView the view
         */
        public IndivConnectionViewHolder(@NonNull View itemView) {
            super(itemView);
            FragmentCreateChatCardBinding binding = FragmentCreateChatCardBinding.bind(itemView);
            mName = binding.textName;
            mSelect = binding.buttonSelect;
            mSelect.setOnClickListener(button -> {
                if (mSelect.isChecked()) {
                    mLastCheckedPos = getAdapterPosition();
                    notifyDataSetChanged();
                }

                Log.d("IndivConnectionViewHolder", "last checked position: " + mLastCheckedPos);
            });

        }
    }

}
