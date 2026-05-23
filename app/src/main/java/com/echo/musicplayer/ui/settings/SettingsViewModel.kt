package com.echo.musicplayer.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echo.musicplayer.domain.model.AppSettings
import com.echo.musicplayer.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val state: StateFlow<SettingsUiState> = settingsRepository.settings
        .map { SettingsUiState(settings = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setPrimaryColor(argb: Long) {
        viewModelScope.launch { settingsRepository.setPrimaryColor(argb) }
    }

    fun setDownloadOverWifiOnly(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDownloadOverWifiOnly(enabled) }
    }

    fun setKeepScreenOnWhilePlaying(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setKeepScreenOnWhilePlaying(enabled) }
    }
}
