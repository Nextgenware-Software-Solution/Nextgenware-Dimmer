package com.nextgenware.dimmer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nextgenware.dimmer.data.repository.BrightnessRepository

class BrightnessViewModelFactory(
    private val repository: BrightnessRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BrightnessViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BrightnessViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
