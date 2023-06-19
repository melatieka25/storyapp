package com.example.storyapp.view.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.event.Event
import com.example.storyapp.preferences.UserPreference
import com.example.storyapp.response_model.UserLoginResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class LoginViewModel(private val pref: UserPreference) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _snackbarText = MutableLiveData<Event<String>>()
    val snackbarText: LiveData<Event<String>> = _snackbarText

    private val _isConnectionFailed = MutableLiveData<Event<Boolean>>()
    val isConnectionFailed: LiveData<Event<Boolean>> = _isConnectionFailed

    fun loginUser(email: String, password: String, successMessage: String, failedMessage: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().loginUser(email, password)
        client.enqueue(object : retrofit2.Callback<UserLoginResponse> {
            override fun onResponse(
                call: Call<UserLoginResponse>,
                response: Response<UserLoginResponse>
            ) {
                _isConnectionFailed.value = Event(false)
                _isLoading.value = false
                val responseBody = response.body()
                if (response.isSuccessful) {
                    if (responseBody != null) {
                        viewModelScope.launch {
                            responseBody.loginResult?.let {
                                pref.login(it)
                            }
                            _snackbarText.value = Event(successMessage)
                        }
                    }
                } else {
                    _snackbarText.value = Event(failedMessage)
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UserLoginResponse>, t: Throwable) {
                _isConnectionFailed.value = Event(true)
                _isLoading.value = false
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}