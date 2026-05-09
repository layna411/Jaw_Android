package com.simats.newjaw.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.newjaw.data.network.Doctor
import com.simats.newjaw.data.network.DoctorCreate
import com.simats.newjaw.data.network.DoctorLogin
import com.simats.newjaw.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentUser = MutableStateFlow<Doctor?>(null)
    val currentUser: StateFlow<Doctor?> = _currentUser.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val loginRequest = DoctorLogin(email, password)
                val response = RetrofitClient.api.login(loginRequest)
                _currentUser.value = response.doctor
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
    
    fun logout(onSuccess: () -> Unit) {
        _currentUser.value = null
        onSuccess()
    }
}
