package com.example.wintercamp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wintercamp.Injector
import com.example.wintercamp.repository.AppSettingsRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val appSettingsRepository: AppSettingsRepository = Injector.appSettingsRepository
) : ViewModel() {

    private val _rebootOption = MutableStateFlow(false)
    val rebootOption = _rebootOption.asStateFlow()

    private val _action = MutableStateFlow<MainUiAction>(MainUiAction.Empty)
    val action = _action.asStateFlow()

    init {
        _rebootOption.value = appSettingsRepository.getRebootStatus()
    }

    fun processUiEvent(uiEvent: MainUiEvent) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.e("MainVM", "Error on $uiEvent", throwable)
        }) {
            when (uiEvent) {
                MainUiEvent.Start -> onStart()
                is MainUiEvent.SetRebootState -> setReboot(uiEvent.enable)
            }
        }
    }

    fun processAction(action: MainUiAction) {
        _action.update {
            action
        }
    }

    private suspend fun onStart() = withContext(Dispatchers.IO) {
        _rebootOption.value = appSettingsRepository.getRebootStatus()
    }

    private suspend fun setReboot(enable: Boolean) = withContext(Dispatchers.IO) {
        runCatching {
            appSettingsRepository.setRebootOption(enable)
        }.onSuccess {
            _rebootOption.value = enable
        }.onFailure {
            Log.e("MainVM", "Error on onReboot", it)
        }
    }
}

sealed interface MainUiEvent {
    object Start : MainUiEvent
    data class SetRebootState(val enable: Boolean) : MainUiEvent
}


sealed interface MainUiAction {
    object Empty : MainUiAction
    object StartService : MainUiAction
    object StopService : MainUiAction
}

