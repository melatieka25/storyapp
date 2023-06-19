package com.example.storyapp.api

import com.example.storyapp.response_model.FileUploadResponse
import com.example.storyapp.response_model.GetStoriesResponse
import com.example.storyapp.response_model.UserLoginResponse
import com.example.storyapp.response_model.UserRegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("login")
    fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<UserLoginResponse>

    @FormUrlEncoded
    @POST("register")
    fun registerUser(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<UserRegisterResponse>

    @Multipart
    @POST("stories")
    fun uploadImage(
        @Header("Authorization") authorization: String,
        @Part photo: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: RequestBody?,
        @Part("lon") lon: RequestBody?
    ): Call<FileUploadResponse>

    @GET("stories")
    fun getStories(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<GetStoriesResponse>

    @GET("stories")
    fun getStoriesWithLocation(
        @Header("Authorization") authorization: String, @Query("location") location: Int
    ): Call<GetStoriesResponse>
}