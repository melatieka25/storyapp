package com.example.storyapp.view.add_story

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.event.Event
import com.example.storyapp.response_model.FileUploadResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class AddStoryViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _snackbarText = MutableLiveData<Event<String>>()
    val snackbarText: LiveData<Event<String>> = _snackbarText

    private val _isConnectionFailed = MutableLiveData<Event<Boolean>>()
    val isConnectionFailed: LiveData<Event<Boolean>> = _isConnectionFailed

    fun addStory(
        token: String,
        file: File,
        description: String,
        successMessage: String,
        failedMessage: String
    ) {
        val descToRequest = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = file.asRequestBody("image/jpeg".toMediaType())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            file.name,
            requestImageFile
        )
        _isLoading.value = true
        _isConnectionFailed.value = Event(false)
        val apiService = ApiConfig.getApiService()
        val uploadImageRequest =
            apiService.uploadImage("Bearer $token", imageMultipart, descToRequest, null, null)
        uploadImageRequest.enqueue(object : Callback<FileUploadResponse> {
            override fun onResponse(
                call: Call<FileUploadResponse>,
                response: Response<FileUploadResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        _snackbarText.value = Event(successMessage)
                    }
                } else {
                    _snackbarText.value = Event(failedMessage)
                }
            }

            override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                _isLoading.value = false
                _isConnectionFailed.value = Event(true)
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    fun addStoryWithLocation(
        token: String,
        file: File,
        description: String,
        latitude: Double,
        longitude: Double,
        successMessage: String,
        failedMessage: String
    ) {
        val descToRequest = description.toRequestBody("text/plain".toMediaType())
        val latitudeToRequest = latitude.toString().toRequestBody("text/plain".toMediaType())
        val longitudeToRequest = longitude.toString().toRequestBody("text/plain".toMediaType())
        val requestImageFile = file.asRequestBody("image/jpeg".toMediaType())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            file.name,
            requestImageFile
        )
        _isLoading.value = true
        _isConnectionFailed.value = Event(false)
        val apiService = ApiConfig.getApiService()
        val uploadImageRequest = apiService.uploadImage(
            "Bearer $token", imageMultipart, descToRequest,
            latitudeToRequest, longitudeToRequest
        )
        uploadImageRequest.enqueue(object : Callback<FileUploadResponse> {
            override fun onResponse(
                call: Call<FileUploadResponse>,
                response: Response<FileUploadResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        _snackbarText.value = Event(successMessage)
                    }
                } else {
                    _snackbarText.value = Event(failedMessage)
                }
            }

            override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                _isLoading.value = false
                _isConnectionFailed.value = Event(true)
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    companion object {
        const val TAG = "AddStoryViewModel"
    }
}