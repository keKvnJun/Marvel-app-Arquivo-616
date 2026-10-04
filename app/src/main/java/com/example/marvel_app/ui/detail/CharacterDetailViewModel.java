package com.example.marvel_app.ui.detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.marvel_app.data.model.CharacterDto;
import com.example.marvel_app.data.repository.CharacterRepository;
import com.example.marvel_app.data.repository.RepositoryResult;

import retrofit2.Call;

public final class CharacterDetailViewModel extends AndroidViewModel {
    private final MutableLiveData<RepositoryResult<CharacterDto>> result = new MutableLiveData<>();
    private final CharacterRepository repository;
    private Call<?> activeCall;

    public CharacterDetailViewModel(@NonNull Application application) {
        super(application);
        repository = CharacterRepository.getInstance(application);
    }

    public LiveData<RepositoryResult<CharacterDto>> result() { return result; }

    public void load(String objectId) {
        if (objectId == null || objectId.trim().isEmpty()) {
            return;
        }
        if (activeCall != null) activeCall.cancel();
        activeCall = repository.loadCharacter(objectId, result::postValue);
    }

    @Override
    protected void onCleared() {
        if (activeCall != null) activeCall.cancel();
    }
}
