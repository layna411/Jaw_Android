package com.simats.newjaw.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.simats.newjaw.data.network.Doctor
import com.simats.newjaw.data.network.DoctorCreate
import com.simats.newjaw.data.network.DoctorLogin
import com.simats.newjaw.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentUser = MutableStateFlow<Doctor?>(null)
    val currentUser: StateFlow<Doctor?> = _currentUser.asStateFlow()

    init {
        // Load saved user if exists
        val savedUserJson = sharedPreferences.getString("current_user", null)
        if (savedUserJson != null) {
            try {
                _currentUser.value = gson.fromJson(savedUserJson, Doctor::class.java)
            } catch (e: Exception) {
                sharedPreferences.edit().remove("current_user").apply()
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val loginRequest = DoctorLogin(email, password)
                val response = RetrofitClient.api.login(loginRequest)
                _currentUser.value = response.doctor
                
                // Persist user
                sharedPreferences.edit()
                    .putString("current_user", gson.toJson(response.doctor))
                    .apply()
                
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Login failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(
        fullName: String,
        email: String,
        phone: String,
        hospital: String,
        specialization: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val registerRequest = DoctorCreate(
                    full_name = fullName,
                    email = email,
                    phone = phone,
                    hospital_name = hospital,
                    specialization = specialization,
                    password = password
                )
                RetrofitClient.api.register(registerRequest)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Registration failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(doctor: Doctor, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                RetrofitClient.api.updateDoctor(doctor)
                _currentUser.value = doctor
                
                // Persist updated user
                sharedPreferences.edit()
                    .putString("current_user", gson.toJson(doctor))
                    .apply()
                
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Update failed"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun logout(onSuccess: () -> Unit) {
        _currentUser.value = null
        sharedPreferences.edit().remove("current_user").apply()
        onSuccess()
    }
}
