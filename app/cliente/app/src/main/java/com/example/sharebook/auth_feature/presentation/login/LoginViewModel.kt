package com.example.sharebook.auth_feature.presentation.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharebook.auth_feature.domain.usecase.LoginUseCase
import com.example.sharebook.auth_feature.presentation.login.state.LoginRequestState
import com.example.sharebook.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.sharebook.auth_feature.presentation.login.event.LoginFormEvent
import com.example.sharebook.auth_feature.presentation.login.state.LoginFormState
import com.example.sharebook.auth_feature.domain.usecase.validations.ValidateUseCases
import com.example.sharebook.auth_feature.presentation.login.state.toLoginModel
import com.example.sharebook.core.utils.UiText

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val validateUseCases: ValidateUseCases
): ViewModel() {
    var uiRequestState by mutableStateOf(LoginRequestState())
        private set

    var uiFormState by mutableStateOf(LoginFormState())
        private set


    fun onEvent(event: LoginFormEvent) {
        when(event) {
            is LoginFormEvent.EmailChanged -> {
                uiFormState = uiFormState.copy(email = event.email)
            }
            is LoginFormEvent.PasswordChanged -> {
                uiFormState = uiFormState.copy(password = event.password)
            }
            is LoginFormEvent.Submit -> {
                submitForm()
            }
        }
    }

    private fun submitForm() {
        val validateEmailResult = validateUseCases.validateEmailUseCase(uiFormState.email)
        val validatePasswordResult = validateUseCases.validatePasswordUseCase(uiFormState.password)

        val hasError = listOf(validateEmailResult, validatePasswordResult).any { !it.isValid }

        if (hasError) {
            uiFormState = uiFormState.copy(
                emailError = UiText.StringResource(resId = validateEmailResult.resId),
                passwordError = UiText.StringResource(resId = validatePasswordResult.resId)
            )
            return
        }

        uiFormState = uiFormState.copy(
            emailError = UiText.DynamicText(""),
            passwordError = UiText.DynamicText("")
        )

        login()
    }

    fun login() {
        viewModelScope.launch {
            loginUseCase(uiFormState.toLoginModel()).collect { result ->
                uiRequestState = when(result) {
                    is Resource.Success -> {
                        uiRequestState.copy(success = true, isLoading = false)
                    }
                    is Resource.Error -> {
                        uiRequestState.copy(error = result.message, isLoading = false)
                    }
                    is Resource.Loading -> {
                        uiRequestState.copy(isLoading = true)
                    }
                }
            }
        }
    }
}
