package com.example.a216553_praavieen_rajj_nelson_lab04


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UserProfile())
    val uiState: StateFlow<UserProfile> = _uiState.asStateFlow()

    fun updateFromLogin(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun updateFromRegister(fullName: String, username: String, email: String, phone: String) {
        _uiState.update {
            it.copy(fullName = fullName, username = username, email = email, phone = phone)
        }
    }

    fun clear() {
        _uiState.value = UserProfile()
    }
}