package edu.uw.tcss450.team_5_tcss_450.ui.connections;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentConnectionsBinding;
import edu.uw.tcss450.team_5_tcss_450.notifications.Notification;
import edu.uw.tcss450.team_5_tcss_450.notifications.NotificationListViewModel;
import edu.uw.tcss450.team_5_tcss_450.model.UserInfoViewModel;

/**
 * The main fragment for viewing a user's connections, from which all other connection actions are
 * linked
 *
 * @author Ismael Jones
 * @version 5/19/21
 */
public class ConnectionsFragment extends Fragment {
    private FragmentConnectionsBinding mBinding;
    private ConnectionsViewModel mModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mModel = new ViewModelProvider(getActivity()).get(ConnectionsViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mModel = new ViewModelProvider(getActivity()).get(ConnectionsViewModel.class);
        UserInfoViewModel userModel = new ViewModelProvider(getActivity()).get(UserInfoViewModel.class);
        mModel.getUserID(userModel.getEmail());
        mModel.addUserIDObserver(this , id -> {
            if (!(id == 0)) {
                Log.d("connections", "" + id);
                mModel.getConnections(id);
            }
        });

        ViewModelProvider provider = new ViewModelProvider(getActivity());
        mModel = provider.get(ConnectionsViewModel.class);
        UserInfoViewModel userInfoViewModel = provider.get(UserInfoViewModel.class);

        return inflater.inflate(R.layout.fragment_connections, container, false);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.connections_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Navigate based on the button selection from the app bar
        switch (item.getItemId()) {
            case R.id.connections_search: {

                //actionNavigationContactsToAddConnectionFragment2
                Navigation.findNavController(getView()).navigate(ConnectionsFragmentDirections.actionNavigationContactsToAddConnectionFragment());
                return super.onOptionsItemSelected(item);
            }
            case R.id.connections_requests: {
                Log.d("test", "test");

//                 actionNavigationContactsToConnectionRequestFragment2
                Navigation.findNavController(getView()).navigate(ConnectionsFragmentDirections.actionNavigationContactsToConnectionRequestFragment());
                return super.onOptionsItemSelected(item);
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = FragmentConnectionsBinding.bind(getView());

        mModel.addConnectionListObserver(getViewLifecycleOwner(), connectionList -> {
                if(!connectionList.isEmpty()){
                    mBinding.layoutEmpty.setVisibility(View.GONE);
                }
                mBinding.listRoot.setAdapter(
                        new ConnectionsRecyclerViewAdapter(connectionList));


        });
    }
}