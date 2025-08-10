package com.example.solverpoker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solverpoker.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HintViewModel @Inject constructor(private val preferences: AppPreferences) : ViewModel() {

    private val _showAppHint = MutableStateFlow(false)
    val showAppHint: StateFlow<Boolean> = _showAppHint.asStateFlow()

    private val _showScreenHint = MutableStateFlow(false)
    val showScreenHint: StateFlow<Boolean> = _showScreenHint.asStateFlow()

    init {
        // Проверка первого запуска приложения
        viewModelScope.launch {
            preferences.isAppFirstLaunch.collect { isFirstLaunch ->
                _showAppHint.value = isFirstLaunch
            }
        }
    }

    fun checkScreenHint(screenName: String) {
        viewModelScope.launch {
            preferences.isScreenHintShown(screenName).collect { isShown ->
                _showScreenHint.value = !isShown
            }
        }
    }

    fun markAppHintShown() {
        viewModelScope.launch {
            preferences.setAppFirstLaunch(false)
            _showAppHint.value = false
        }
    }

    fun markScreenHintShown(screenName: String) {
        viewModelScope.launch {
            preferences.setScreenHintShown(screenName, true)
            _showScreenHint.value = false
        }
    }
}