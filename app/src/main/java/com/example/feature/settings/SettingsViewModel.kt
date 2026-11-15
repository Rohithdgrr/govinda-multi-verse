package com.example.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Difficulty
import com.example.domain.model.UserPreferences
import com.example.domain.repository.MathLearningRepository
import com.example.domain.usecase.ExportLearningDataUseCase
import com.example.domain.usecase.ResetLearningProfileUseCase
import com.example.lock.GovindaQuizLockService
import com.example.lock.InstalledAppItem
import com.example.lock.InstalledAppsProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val installedApps: List<InstalledAppItem> = emptyList(),
    val isLoadingApps: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val isOverlayEnabled: Boolean = false,
    val exportedJsonPreview: String? = null,
    val statusMessage: String? = null
) {
    val isAllLockPermissionsGranted: Boolean
        get() = isAccessibilityEnabled && isOverlayEnabled
}

private data class AppsLockMeta(
    val installedApps: List<InstalledAppItem> = emptyList(),
    val isLoadingApps: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val isOverlayEnabled: Boolean = false
)

class SettingsViewModel(
    private val repository: MathLearningRepository,
    private val exportLearningData: ExportLearningDataUseCase,
    private val resetLearningProfile: ResetLearningProfileUseCase
) : ViewModel() {

    private val exportedJson = MutableStateFlow<String?>(null)
    private val statusMessage = MutableStateFlow<String?>(null)
    private val appsLockMeta = MutableStateFlow(AppsLockMeta())

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.observePreferences(),
        appsLockMeta,
        exportedJson,
        statusMessage
    ) { prefs, appsMeta, json, msg ->
        SettingsUiState(
            preferences = prefs,
            installedApps = appsMeta.installedApps,
            isLoadingApps = appsMeta.isLoadingApps,
            isAccessibilityEnabled = appsMeta.isAccessibilityEnabled,
            isOverlayEnabled = appsMeta.isOverlayEnabled,
            exportedJsonPreview = json,
            statusMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    private var loadAppsJob: Job? = null

    fun refreshAppLockEnvironment(context: Context) {
        val serviceEnabled = GovindaQuizLockService.isServiceEnabled(context)
        val overlayEnabled = GovindaQuizLockService.isOverlayEnabled(context)
        val shouldLoadApps = appsLockMeta.value.installedApps.isEmpty() && loadAppsJob?.isActive != true
        appsLockMeta.value = appsLockMeta.value.copy(
            isAccessibilityEnabled = serviceEnabled,
            isOverlayEnabled = overlayEnabled,
            isLoadingApps = shouldLoadApps || loadAppsJob?.isActive == true
        )
        if (shouldLoadApps) {
            loadAppsJob = viewModelScope.launch {
                val apps = runCatching {
                    withTimeoutOrNull(5_000L) {
                        InstalledAppsProvider.loadLaunchableApps(context)
                    }
                }.getOrNull()

                if (apps == null) {
                    statusMessage.value = "Timed out loading installed apps. Reopen selector to retry."
                }
                appsLockMeta.value = appsLockMeta.value.copy(
                    installedApps = apps ?: appsLockMeta.value.installedApps,
                    isLoadingApps = false,
                    isAccessibilityEnabled = GovindaQuizLockService.isServiceEnabled(context),
                    isOverlayEnabled = GovindaQuizLockService.isOverlayEnabled(context)
                )
            }
        }
    }

    fun setQuizLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(quizLockEnabled = enabled) }
        }
    }

    fun setDeveloperModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(developerModeEnabled = enabled) }
        }
    }

    fun toggleLockedPackage(packageName: String) {
        viewModelScope.launch {
            repository.updatePreferences { prefs ->
                val updated = prefs.quizLockedPackages.toMutableSet()
                if (!updated.add(packageName)) {
                    updated.remove(packageName)
                }
                val autoEnable = if (updated.isNotEmpty()) true else prefs.quizLockEnabled
                prefs.copy(
                    quizLockedPackages = updated,
                    quizLockEnabled = autoEnable
                )
            }
        }
    }

    fun clearLockedPackages() {
        viewModelScope.launch {
            repository.updatePreferences {
                it.copy(quizLockedPackages = emptySet(), quizLockEnabled = false)
            }
        }
    }


    fun setAllowNegativeSubtraction(allow: Boolean) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(allowNegativeSubtraction = allow) }
        }
    }

    fun setManualDifficultyOverride(difficulty: Difficulty?) {
        viewModelScope.launch {
            repository.updatePreferences { it.copy(manualDifficultyOverride = difficulty) }
        }
    }

    fun setMinDifficulty(difficulty: Difficulty) {
        viewModelScope.launch {
            repository.updatePreferences { prefs ->
                val adjustedMax = if (prefs.maxDifficulty.level < difficulty.level) difficulty else prefs.maxDifficulty
                prefs.copy(minDifficulty = difficulty, maxDifficulty = adjustedMax)
            }
        }
    }

    fun setMaxDifficulty(difficulty: Difficulty) {
        viewModelScope.launch {
            repository.updatePreferences { prefs ->
                val adjustedMin = if (prefs.minDifficulty.level > difficulty.level) difficulty else prefs.minDifficulty
                prefs.copy(minDifficulty = adjustedMin, maxDifficulty = difficulty)
            }
        }
    }

    fun generateExport() {
        viewModelScope.launch {
            val json = exportLearningData()
            exportedJson.value = json
        }
    }

    fun dismissExportPreview() {
        exportedJson.value = null
    }

    fun resetAllData() {
        viewModelScope.launch {
            resetLearningProfile()
            statusMessage.value = "All local learning profiles and history reset."
        }
    }

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    class Factory(
        private val repository: MathLearningRepository,
        private val exportLearningData: ExportLearningDataUseCase,
        private val resetLearningProfile: ResetLearningProfileUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository, exportLearningData, resetLearningProfile) as T
        }
    }
}
