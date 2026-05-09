package com.simats.newjaw.data.network

data class DoctorLogin(
    val email: String,
    val password: String
)

data class DoctorCreate(
    val full_name: String,
    val email: String,
    val phone: String,
    val hospital_name: String,
    val specialization: String,
    val password: String
)

data class Doctor(
    val id: Int,
    val full_name: String,
    val email: String,
    val phone: String,
    val hospital_name: String,
    val specialization: String
)

data class LoginResponse(
    val message: String,
    val doctor: Doctor
)

data class RegisterResponse(
    val message: String
)

data class Patient(
    val id: Int,
    val doctor_id: Int,
    val patient_name: String,
    val age: Int?,
    val gender: String?,
    val phone: String?,
    val medical_condition: String?,
    val assigned_exercise: String?,
    val created_at: String
)

data class PatientCreate(
    val doctor_id: Int,
    val patient_name: String,
    val age: Int? = null,
    val gender: String? = null,
    val phone: String? = null,
    val medical_condition: String? = null,
    val assigned_exercise: String? = null
)

data class BaseResponse(
    val message: String
)

data class SessionData(
    val session_date: String,
    val max_disp: Double,
    val avg_velocity: Double,
    val max_rom: Double,
    val avg_symmetry: Double
)
