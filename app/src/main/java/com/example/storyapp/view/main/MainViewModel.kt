package com.example.storyapp.view.main

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.event.Event
import com.example.storyapp.preferences.UserPreference
import com.example.storyapp.response_model.ListStoryItem
import com.example.storyapp.response_model.LoginResult
import kotlinx.coroutines.launch

class MainViewModel(
    private val pref: UserPreference,
    private val storyRepository: StoryRepository
) : ViewModel() {

    val story: LiveData<PagingData<ListStoryItem>> =
        storyRepository.getStory().cachedIn(viewModelScope)

    private val _snackbarText = MutableLiveData<Event<String>>()
    val snackbarText: LiveData<Event<String>> = _snackbarText

    private val _isConnectionFailed = MutableLiveData<Event<Boolean>>()
    val isConnectionFailed: LiveData<Event<Boolean>> = _isConnectionFailed

    fun getUser(): LiveData<LoginResult> {
        return pref.getUser().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            pref.logout()
        }
    }

    fun setSuccessLogin(successMessage: String) {
        _snackbarText.value = Event(successMessage)
    }

    fun setSuccessUpload(successMessage: String) {
        _snackbarText.value = Event(successMessage)
    }

    fun setConnectionFailed(isFailed: Boolean) {
        _isConnectionFailed.value = Event(isFailed)
    }

}