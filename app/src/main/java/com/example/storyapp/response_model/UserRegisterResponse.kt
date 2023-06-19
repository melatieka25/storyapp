package com.example.storyapp.response_model

import com.google.gson.annotations.SerializedName

data class UserRegisterResponse(

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null
)
