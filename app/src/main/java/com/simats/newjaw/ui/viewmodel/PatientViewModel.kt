package com.simats.newjaw.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.newjaw.data.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PatientViewModel : ViewModel() {
    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchPatients(doctorId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.api.getPatients(doctorId)
                _patients.value = response
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to fetch patients"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addPatient(patient: PatientCreate, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                RetrofitClient.api.addPatient(patient)
                fetchPatients(patient.doctor_id)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to add patient"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
