package com.gumkid.flashcard.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.gumkid.flashcard.repository.AuthRepository
import com.gumkid.flashcard.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        if (authRepository.isUserLoggedIn) {
            _authState.value = AuthState.Authenticated(authRepository.currentUser!!)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signInWithEmailAndPassword(email, password)
            _isLoading.value = false

            result.fold(
                onSuccess = { user ->
                    _authState.value = AuthState.Authenticated(user)
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Login failed")
                }
            )
        }
    }

    fun signUp(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.createUserWithEmailAndPassword(email, password)
            _isLoading.value = false

            result.fold(
                onSuccess = { user ->
                    _authState.value = AuthState.Authenticated(user)
                    NotificationHelper.showFlashcardNotification(
                        context,
                        "Registration Successful",
                        "Welcome! Your account has been created successfully"
                    )
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Registration failed")
                }
            )
        }
    }

    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun resetAuthState() {
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}
