package com.example.todoandroidapp

import retrofit2.http.GET

interface ApiService {
    @GET("api/v1/:endpoint")
    suspend fun getData(): List<ResponseDataItem>
}
