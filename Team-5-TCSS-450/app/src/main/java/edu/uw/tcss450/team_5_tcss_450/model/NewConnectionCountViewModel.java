package edu.uw.tcss450.team_5_tcss_450.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

public class NewConnectionCountViewModel extends ViewModel {

    private MutableLiveData<Integer> mNewOutgoingCount;
    private MutableLiveData<Integer> mNewIncomingCount;
    private MutableLiveData<Integer> mNewConnectionsCount;

    public NewConnectionCountViewModel() {
        mNewOutgoingCount = new MutableLiveData<>();
        mNewOutgoingCount.setValue(0);
        mNewIncomingCount = new MutableLiveData<>();
        mNewIncomingCount.setValue(0);
        mNewConnectionsCount = new MutableLiveData<>();
        mNewConnectionsCount.setValue(0);

    }

    public void addOutgoingCountObserver(@NonNull LifecycleOwner owner, @NonNull Observer<? super Integer> observer) {
        mNewOutgoingCount.observe(owner, observer);
    }

    public void incrementOutgoing() {
        mNewOutgoingCount.setValue(mNewOutgoingCount.getValue() + 1);
    }

    public void decrementOutgoing() {
        mNewOutgoingCount.setValue(mNewOutgoingCount.getValue() - 1);
    }

    public void resetOutgoing() {
        mNewOutgoingCount.setValue(0);
    }

    public void addIncomingCountObserver(@NonNull LifecycleOwner owner, @NonNull Observer<? super Integer> observer) {
        mNewIncomingCount.observe(owner, observer);
    }

    public void incrementIncoming() {
        mNewIncomingCount.setValue(mNewIncomingCount.getValue() + 1);
    }

    public void decrementIncoming() {
        mNewIncomingCount.setValue(mNewIncomingCount.getValue() - 1);
    }

    public void resetIncoming() {
        mNewIncomingCount.setValue(0);
    }

    public void addConnectionsCountObserver(@NonNull LifecycleOwner owner, @NonNull Observer<? super Integer> observer) {
        mNewConnectionsCount.observe(owner, observer);
    }

    public void incrementConnections() {
        mNewConnectionsCount.setValue(mNewConnectionsCount.getValue() + 1);
    }

    public void decrementConnections() {
        mNewConnectionsCount.setValue(mNewConnectionsCount.getValue() - 1);
    }

    public void resetConnections() {
        mNewConnectionsCount.setValue(0);
    }


}
