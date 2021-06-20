package edu.uw.tcss450.team_5_tcss_450.ui.connections;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentAddConnectionBinding;
import edu.uw.tcss450.team_5_tcss_450.model.UserInfoViewModel;

/**
 * A fragment for sending connection requests and viewing pending requests
 * @author Ismael Jones
 * @version 5/19/21
 */
public class AddConnectionFragment extends Fragment {
    private ConnectionsViewModel mModel;
    private FragmentAddConnectionBinding mBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = new ViewModelProvider(getActivity()).get(ConnectionsViewModel.class);
        UserInfoViewModel userModel = new ViewModelProvider(getActivity()).get(UserInfoViewModel.class);
        mModel.getUserID(userModel.getEmail());
        mModel.addUserIDObserver(this, id -> {
            if (!(id == 0)) {
                mModel.getOutgoingRequests(id);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_connection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = FragmentAddConnectionBinding.bind(getView());
        //Set up the adapter for the list
        mModel.addSentRequestsListObserver(getViewLifecycleOwner(), requestList -> {

            //The llm is required to populate the recyclerview adapter
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            mBinding.recyclerViewSentRequests.setLayoutManager(llm);
            if (!requestList.isEmpty()) {

                mBinding.layoutEmpty.setVisibility(View.GONE);
            }

            mBinding.recyclerViewSentRequests.setAdapter(
                    new SentRequestsRecyclerViewAdapter(requestList));

        });
        mBinding.buttonRequest.setOnClickListener(this::sendRequest);
        //Set up the autocomplete for the search bar
        AutoCompleteTextView autoComplete = mBinding.editTextRequest;
        autoComplete.setThreshold(2);
        autoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Query the db based on the value in the text area
                if(autoComplete.getText().toString().length() > 1){
                    mModel.searchConnections(autoComplete.getText().toString());
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //Set the adapter for the search dropdwon
        mModel.addSearchListObserver(this, connections -> {
            if(!connections.isEmpty()){

                ConnectionsAdapter adapter = new ConnectionsAdapter(getContext(), R.layout.item_connection, connections);
                autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Connection connection = (Connection)parent.getItemAtPosition(position);
                        mBinding.editTextRequest.setText(connection.getEmail());
                    }
                });
                autoComplete.setAdapter(adapter);
                autoComplete.showDropDown();

            }

        });

    }

    /**
     * A method to send a connection request to the userid attached to the view
     * @param view the specific list item representing the connection
     * */
    private void sendRequest(View view) {

        if(mBinding.editTextRequest.getText().toString().contains("@")){
            UserInfoViewModel userModel = new ViewModelProvider(getActivity()).get(UserInfoViewModel.class);
            mModel.getUserID(userModel.getEmail());
            mModel.getSecondID(mBinding.editTextRequest.getText().toString());
            mModel.addUserIDObserver(getViewLifecycleOwner(), id -> {

                if (!(id == 0)) {
                    mModel.addSecondIDObserver(getViewLifecycleOwner(), secondID -> {

                        if (!(secondID == 0) && !(id == secondID)) {

                            mModel.sendRequest(id, secondID);
                        }
                    });
                }
            });

        }
        else{
            mBinding.editTextRequest.setError("Please Enter A valid Email or select one of the options");
        }

    }
}