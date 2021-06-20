package edu.uw.tcss450.team_5_tcss_450.ui.connections;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentConnectionsBinding;
import edu.uw.tcss450.team_5_tcss_450.model.UserInfoViewModel;

/**
 * A fragment to view a user's incoming connection requests.
 * @author Ismael Jones
 * @version 5/19/21
 */
public class ConnectionRequestFragment extends Fragment {
    private edu.uw.tcss450.team_5_tcss_450.databinding.FragmentConnectionRequestBinding mBinding;
    private ConnectionsViewModel mModel;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = new ViewModelProvider(getActivity()).get(ConnectionsViewModel.class);
        UserInfoViewModel userModel = new ViewModelProvider(getActivity()).get(UserInfoViewModel.class);
        mModel.getUserID(userModel.getEmail());
        //Wait for the userid to get requests
        mModel.addUserIDObserver(this, id -> {
            if (!(id == 0)) {
                mModel.getIncomingRequests(id);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connection_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = edu.uw.tcss450.team_5_tcss_450.databinding.FragmentConnectionRequestBinding.bind(getView());
        //Wait for the requests to populate the list
        mModel.addRequestListObserver(getViewLifecycleOwner(), requestList -> {
            if(!requestList.isEmpty()){
                mBinding.layoutEmpty.setVisibility(View.GONE);
            }
            mBinding.listRoot.setAdapter(
                    new ConnectionRequestRecyclerViewAdapter(requestList));

        });
    }
}