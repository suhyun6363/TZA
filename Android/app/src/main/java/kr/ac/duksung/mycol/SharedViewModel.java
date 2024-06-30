// SharedViewModel.java
package kr.ac.duksung.mycol;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> scannedResult = new MutableLiveData<>();

    public void setScannedResult(String result) {
        scannedResult.setValue(result);
    }

    public LiveData<String> getScannedResult() {
        return scannedResult;
    }
}
