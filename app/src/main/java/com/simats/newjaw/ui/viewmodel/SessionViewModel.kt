package com.simats.newjaw.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.simats.newjaw.data.network.RetrofitClient
import com.simats.newjaw.data.network.SessionData

class SessionViewModel : ViewModel() {
    private val _sessions = MutableStateFlow<List<SessionData>>(emptyList())
    val sessions: StateFlow<List<SessionData>> = _sessions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _selectedSession = MutableStateFlow<SessionData?>(null)
    val selectedSession: StateFlow<SessionData?> = _selectedSession.asStateFlow()

    fun selectSession(session: SessionData) {
        _selectedSession.value = session
    }

    fun fetchSessionsForPatient(patientId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.api.getSessions(patientId.toString())
                _sessions.value = response
            } catch (e: Exception) {
                _error.value = "Failed to fetch reports: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
