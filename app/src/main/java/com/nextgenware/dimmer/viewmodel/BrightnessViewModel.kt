package com.nextgenware.dimmer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextgenware.dimmer.data.repository.BrightnessRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BrightnessViewModel(
    private val repository: BrightnessRepository
) : ViewModel() {

    val brightness: StateFlow<Float> = repository.brightness
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.5f
        )

    val isDimmerEnabled: StateFlow<Boolean> = repository.isDimmerEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isNotificationLocked: StateFlow<Boolean> = repository.isNotificationLocked
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val isNotificationEnabled: StateFlow<Boolean> = repository.isNotificationEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val isNotificationSwiped: StateFlow<Boolean> = repository.isNotificationSwiped
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun updateBrightness(value: Float) {
        viewModelScope.launch {
            repository.saveBrightness(value)
        }
    }

    fun toggleDimmer(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDimmerEnabled(enabled)
        }
    }

    fun setNotificationLocked(locked: Boolean) {
        viewModelScope.launch {
            repository.setNotificationLocked(locked)
        }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationEnabled(enabled)
        }
    }

    fun restoreNotification() {
        viewModelScope.launch {
            repository.setNotificationSwiped(false)
            repository.setNotificationEnabled(true)
        }
    }
}
