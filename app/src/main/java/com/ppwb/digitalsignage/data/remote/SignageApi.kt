package com.ppwb.digitalsignage.data.remote

import com.ppwb.digitalsignage.data.remote.dto.SignageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SignageApi {
    @GET("api/devices/{serial_number}/sync")
    suspend fun sync(
        @Path("serial_number") serialNumber: String,
        @Query("current_version") currentVersion: String?
    ): Response<SignageResponse>
}
