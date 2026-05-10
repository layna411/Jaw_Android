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
    suspend fun addPatient(@Body request: PatientCreate): AddPatientResponse

    @GET("patients")
    suspend fun getPatients(@Query("doctor_id") doctorId: Int): List<Patient>

    @GET("sessions")
    suspend fun getSessions(@Query("patient_id") patientId: String): List<SessionData>
    
    @GET("stats")
    suspend fun getStats(@Query("doctor_id") doctorId: Int): DashboardStats


    @POST("auth/update-doctor")
    suspend fun updateDoctor(@Body doctor: Doctor): BaseResponse

    @POST("reset")
    suspend fun resetState(): BaseResponse
}
