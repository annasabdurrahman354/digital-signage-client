package com.ppwb.digitalsignage.presentation

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ppwb.digitalsignage.domain.model.*
import com.ppwb.digitalsignage.domain.repository.SignageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SignageViewModel @Inject constructor(
    private val repository: SignageRepository,
    application: Application
) : ViewModel() {

    val androidId: String = Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)

    private val _uiState = MutableStateFlow<SignageUiState>(SignageUiState.Loading)
    val uiState: StateFlow<SignageUiState> = _uiState.asStateFlow()

    private val _currentAsset = MutableStateFlow<Asset?>(null)
    val currentAsset: StateFlow<Asset?> = _currentAsset.asStateFlow()

    private var signageData: SignageData? = null

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.getSignageData().collect { data ->
                signageData = data
                if (data != null) {
                    _uiState.value = SignageUiState.Success(data)
                    startPlaybackLoop()
                } else {
                    _uiState.value = SignageUiState.Error("No data available. Check connection.")
                }
            }
        }
    }

    private fun startPlaybackLoop() {
        viewModelScope.launch {
            while (true) {
                val data = signageData ?: break
                val activePlaylistId = getActivePlaylistId(data)
                val playlist = data.playlists.find { it.id == activePlaylistId }
                
                if (playlist != null && playlist.items.isNotEmpty()) {
                    for (item in playlist.items.sortedBy { it.order }) {
                        val asset = data.assets.find { it.id == item.assetId }
                        if (asset != null && asset.localPath != null) {
                            _currentAsset.value = asset
                            delay(item.durationSec * 1000L)
                        }
                    }
                } else {
                    delay(5000L) // Wait if no playlist is active
                }
            }
        }
    }

    private fun getActivePlaylistId(data: SignageData): String {
        val now = Calendar.getInstance()
        val today = (now.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1 // Convert to 1=Mon, ..., 7=Sun
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now.time)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)

        val activeSchedules = data.schedules.filter { s ->
            currentDate >= s.validFromDate && currentDate <= s.validUntilDate &&
            today in s.daysOfWeek &&
            isTimeInRange(currentTime, s.timeStart, s.timeEnd)
        }.sortedByDescending { it.priority }

        return activeSchedules.firstOrNull()?.playlistId ?: data.device.defaultPlaylistId
    }

    private fun isTimeInRange(current: String, start: String, end: String): Boolean {
        return if (start <= end) {
            current in start..end
        } else {
            // Over midnight
            current >= start || current <= end
        }
    }
}

sealed class SignageUiState {
    object Loading : SignageUiState()
    data class Success(val data: SignageData) : SignageUiState()
    data class Error(val message: String) : SignageUiState()
}
