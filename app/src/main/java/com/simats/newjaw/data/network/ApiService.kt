package com.simats.newjaw.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: DoctorCreate): RegisterResponse

    @POST("auth/login")
    suspend fun login(@Body request: DoctorLogin): LoginResponse

    @POST("patients")
    suspend fun addPatient(@Body request: PatientCreate): BaseResponse

    @GET("patients")
    suspend fun getPatients(@Query("doctor_id") doctorId: Int): List<Patient>

    @GET("sessions")
    suspend fun getSessions(@Query("patient_id") patientId: String): List<SessionData>
}
