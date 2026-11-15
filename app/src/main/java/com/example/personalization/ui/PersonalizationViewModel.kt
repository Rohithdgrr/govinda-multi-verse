package com.example.personalization.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.personalization.model.AppearanceProfile
import com.example.personalization.model.CustomColorOverrides
import com.example.personalization.model.PersonalizationState
import com.example.personalization.model.ThemeDefinition
import com.example.personalization.repository.PersonalizationRepository
import com.example.personalization.theme.ThemeManager
import com.example.personalization.theme.ThemeRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class PersonalizationSection(val title: String) {
    THEMES("Themes"),
    COLORS("Colors"),
    TYPOGRAPHY("Typography"),
    ICONS("Icons"),
    AVATARS("Avatars"),
    LAYOUTS("Layouts"),
    COMPONENTS("Components"),
    BACKGROUNDS("Backgrounds"),
    ANIMATIONS("Animations"),
    SOUND_HAPTICS("Sound & Haptics"),
    PROFILES("Profiles")
}

data class PersonalizationStudioUiState(
    val draftProfile: AppearanceProfile = AppearanceProfile(),
    val savedState: PersonalizationState = PersonalizationState(),
    val selectedSection: PersonalizationSection = PersonalizationSection.THEMES,
    val showLivePreview: Boolean = true,
    val isFullPreviewMode: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val statusBanner: String? = null
)

class PersonalizationViewModel(
    private val repository: PersonalizationRepository
) : ViewModel() {

    private val draftProfile = MutableStateFlow(repository.state.value.activeProfile)
    private val selectedSection = MutableStateFlow(PersonalizationSection.THEMES)
    private val showLivePreview = MutableStateFlow(true)
    private val isFullPreviewMode = MutableStateFlow(false)
    private val statusBanner = MutableStateFlow<String?>(null)
    private var syncedActiveId: String = repository.state.value.activeProfile.id

    init {
        viewModelScope.launch {
            repository.state.collect { repoState ->
                if (repoState.activeProfile.id != syncedActiveId) {
                    syncedActiveId = repoState.activeProfile.id
                    draftProfile.value = repoState.activeProfile
                }
            }
        }
    }

    val uiState: StateFlow<PersonalizationStudioUiState> = combine(
        draftProfile,
        repository.state,
        selectedSection,
        showLivePreview,
        isFullPreviewMode,
        statusBanner
    ) { values ->
        val draft = values[0] as AppearanceProfile
        val saved = values[1] as PersonalizationState
        val section = values[2] as PersonalizationSection
        val previewVisible = values[3] as Boolean
        val fullPreview = values[4] as Boolean
        val banner = values[5] as String?
        PersonalizationStudioUiState(
            draftProfile = draft,
            savedState = saved,
            selectedSection = section,
            showLivePreview = previewVisible,
            isFullPreviewMode = fullPreview,
            hasUnsavedChanges = draft != saved.activeProfile,
            statusBanner = banner
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PersonalizationStudioUiState(
            draftProfile = repository.state.value.activeProfile,
            savedState = repository.state.value
        )
    )

    fun selectSection(section: PersonalizationSection) {
        selectedSection.value = section
    }

    fun toggleLivePreview() {
        showLivePreview.update { !it }
    }

    fun setFullPreviewMode(enabled: Boolean) {
        isFullPreviewMode.value = enabled
    }

    fun clearStatusBanner() {
        statusBanner.value = null
    }

    fun updateDraft(transform: (AppearanceProfile) -> AppearanceProfile) {
        draftProfile.update(transform)
    }

    fun applyChanges() {
        val currentDraft = draftProfile.value
        viewModelScope.launch {
            repository.applyProfile(currentDraft)
            syncedActiveId = currentDraft.id
            statusBanner.value = "Applied \"${currentDraft.name}\" across GOVINDA."
        }
    }

    fun cancelChanges() {
        val active = repository.state.value.activeProfile
        draftProfile.value = active
        syncedActiveId = active.id
        statusBanner.value = "Reverted unsaved preview edits."
    }

    fun resetAllToDefaults() {
        viewModelScope.launch {
            repository.resetAllToDefaults()
            val def = ThemeRegistry.builtInProfiles.first()
            draftProfile.value = def
            syncedActiveId = def.id
            statusBanner.value = "Restored Blueprint Default appearance."
        }
    }

    fun saveDraftAsNewProfile(name: String) {
        viewModelScope.launch {
            val created = repository.saveNewProfile(name, draftProfile.value)
            draftProfile.value = created
            syncedActiveId = created.id
            statusBanner.value = "Saved profile \"${created.name}\"."
        }
    }

    fun switchProfile(profileId: String) {
        viewModelScope.launch {
            repository.switchProfile(profileId)
            val active = repository.state.value.activeProfile
            draftProfile.value = active
            syncedActiveId = active.id
            statusBanner.value = "Switched to \"${active.name}\"."
        }
    }

    fun renameProfile(profileId: String, newName: String) {
        viewModelScope.launch {
            repository.renameProfile(profileId, newName)
            if (draftProfile.value.id == profileId) {
                draftProfile.update { it.copy(name = newName.trim().ifEmpty { it.name }) }
            }
            statusBanner.value = "Renamed profile."
        }
    }

    fun duplicateProfile(profileId: String) {
        viewModelScope.launch {
            val dup = repository.duplicateProfile(profileId)
            if (dup != null) {
                draftProfile.value = dup
                syncedActiveId = dup.id
                statusBanner.value = "Duplicated as \"${dup.name}\"."
            }
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            repository.deleteProfile(profileId)
            val active = repository.state.value.activeProfile
            draftProfile.value = active
            syncedActiveId = active.id
            statusBanner.value = "Deleted custom profile."
        }
    }

    fun saveCurrentAsCustomTheme(name: String, isSystemDark: Boolean) {
        val draft = draftProfile.value
        val resolved = ThemeManager.resolveActiveTheme(
            profile = draft,
            customThemes = repository.state.value.customThemes,
            isSystemDark = isSystemDark
        )
        val newTheme = resolved.copy(
            id = "custom_theme_${UUID.randomUUID().toString().take(8)}",
            name = name.trim().ifBlank { "Custom Theme" },
            isCustom = true
        )
        viewModelScope.launch {
            repository.saveCustomTheme(newTheme)
            draftProfile.update {
                it.copy(
                    themeId = newTheme.id,
                    customColors = CustomColorOverrides(enabled = false)
                )
            }
            statusBanner.value = "Saved custom theme \"${newTheme.name}\"."
        }
    }

    fun renameCustomTheme(themeId: String, newName: String) {
        viewModelScope.launch {
            repository.renameCustomTheme(themeId, newName)
            statusBanner.value = "Renamed custom theme."
        }
    }

    fun deleteCustomTheme(themeId: String) {
        viewModelScope.launch {
            repository.deleteCustomTheme(themeId)
            if (draftProfile.value.themeId == themeId) {
                draftProfile.update { it.copy(themeId = ThemeRegistry.blueprintBlue.id) }
            }
            statusBanner.value = "Deleted custom theme."
        }
    }

    class Factory(
        private val repository: PersonalizationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PersonalizationViewModel(repository) as T
        }
    }
}
