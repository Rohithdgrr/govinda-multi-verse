package com.example.personalization.repository

import com.example.personalization.model.AppearanceProfile
import com.example.personalization.model.PersonalizationState
import com.example.personalization.model.ThemeDefinition
import com.example.personalization.storage.AppearanceDataStore
import com.example.personalization.theme.ThemeRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class PersonalizationRepository(
    private val dataStore: AppearanceDataStore
) {
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow(AppearanceDataStore.defaultState())
    val state: StateFlow<PersonalizationState> = _state.asStateFlow()

    init {
        repoScope.launch {
            dataStore.stateFlow.collect { loaded ->
                _state.value = loaded
            }
        }
    }

    suspend fun applyProfile(profile: AppearanceProfile) {
        val current = _state.value
        val updatedProfiles = current.savedProfiles.map {
            if (it.id == profile.id) profile else it
        }.ifEmpty { ThemeRegistry.builtInProfiles }
        val next = current.copy(
            activeProfile = profile,
            savedProfiles = updatedProfiles
        )
        _state.value = next
        dataStore.saveState(next)
    }

    suspend fun saveNewProfile(name: String, base: AppearanceProfile): AppearanceProfile {
        val created = base.copy(
            id = "custom_${UUID.randomUUID().toString().take(8)}",
            name = name.trim().ifBlank { "Custom Profile" },
            isBuiltIn = false
        )
        val current = _state.value
        val next = current.copy(
            activeProfile = created,
            savedProfiles = current.savedProfiles + created
        )
        _state.value = next
        dataStore.saveState(next)
        return created
    }

    suspend fun renameProfile(profileId: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        val current = _state.value
        val nextProfiles = current.savedProfiles.map {
            if (it.id == profileId && !it.isBuiltIn) it.copy(name = trimmed) else it
        }
        val nextActive = if (current.activeProfile.id == profileId && !current.activeProfile.isBuiltIn) {
            current.activeProfile.copy(name = trimmed)
        } else {
            current.activeProfile
        }
        val next = current.copy(activeProfile = nextActive, savedProfiles = nextProfiles)
        _state.value = next
        dataStore.saveState(next)
    }

    suspend fun duplicateProfile(profileId: String): AppearanceProfile? {
        val current = _state.value
        val source = current.savedProfiles.firstOrNull { it.id == profileId } ?: return null
        return saveNewProfile("${source.name} Copy", source)
    }

    suspend fun switchProfile(profileId: String) {
        val current = _state.value
        val target = current.savedProfiles.firstOrNull { it.id == profileId } ?: return
        val next = current.copy(activeProfile = target)
        _state.value = next
        dataStore.saveState(next)
    }

    suspend fun deleteProfile(profileId: String) {
        val current = _state.value
        val target = current.savedProfiles.firstOrNull { it.id == profileId } ?: return
        if (target.isBuiltIn) return
        val remaining = current.savedProfiles.filterNot { it.id == profileId }
        val nextActive = if (current.activeProfile.id == profileId) {
            remaining.firstOrNull() ?: AppearanceProfile()
        } else {
            current.activeProfile
        }
        val next = current.copy(activeProfile = nextActive, savedProfiles = remaining)
        _state.value = next
        dataStore.saveState(next)
    }

    suspend fun saveCustomTheme(theme: ThemeDefinition) {
        val current = _state.value
        val existingIdx = current.customThemes.indexOfFirst { it.id == theme.id }
        val updatedThemes = if (existingIdx >= 0) {
            current.customThemes.toMutableList().apply { this[existingIdx] = theme }
        } else {
            current.customThemes + theme
        }
        val next = current.copy(
            customThemes = updatedThemes,
            activeProfile = current.activeProfile.copy(themeId = theme.id)
        )
        _state.value = next
        dataStore.saveState(next)
    }

    suspend fun renameCustomTheme(themeId: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        val current = _state.value
        val updated = current.customThemes.map {
            if (it.id == themeId) it.copy(name = trimmed) else it
        }
        val next = current.copy(customThemes = updated)
        _state.value = next
        dataStore.saveState(next)
    }

    suspend fun deleteCustomTheme(themeId: String) {
        val current = _state.value
        val updated = current.customThemes.filterNot { it.id == themeId }
        val nextProfile = if (current.activeProfile.themeId == themeId) {
            current.activeProfile.copy(themeId = ThemeRegistry.blueprintBlue.id)
        } else {
            current.activeProfile
        }
        val next = current.copy(activeProfile = nextProfile, customThemes = updated)
        _state.value = next
        dataStore.saveState(next)
    }

    suspend fun resetAllToDefaults() {
        val defaults = AppearanceDataStore.defaultState()
        _state.value = defaults
        dataStore.saveState(defaults)
    }
}
