package com.hyundailogics.shupool.apitest

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
interface KakaoAPI {
    @GET("v2/local/geo/coord2address.json")
    fun getSearchAddress(
        @Header("Authorization") key : String,
        @Query("x") x : String,
        @Query("y") y : String

    ): Call<ResultAddress>
}