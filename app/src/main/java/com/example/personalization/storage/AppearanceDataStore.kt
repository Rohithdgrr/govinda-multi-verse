package com.example.personalization.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.personalization.model.AppearanceProfile
import com.example.personalization.model.AvatarBadge
import com.example.personalization.model.AvatarBorderStyle
import com.example.personalization.model.AvatarProfile
import com.example.personalization.model.AvatarStyle
import com.example.personalization.model.BackgroundPattern
import com.example.personalization.model.ButtonStylePreset
import com.example.personalization.model.CardStylePreset
import com.example.personalization.model.ChallengeLayoutStyle
import com.example.personalization.model.ComponentStyle
import com.example.personalization.model.CustomColorOverrides
import com.example.personalization.model.DashboardLayoutStyle
import com.example.personalization.model.DashboardWidgetConfig
import com.example.personalization.model.DashboardWidgetId
import com.example.personalization.model.DensityMode
import com.example.personalization.model.EffectSettings
import com.example.personalization.model.ElevationLevel
import com.example.personalization.model.FontWeightLevel
import com.example.personalization.model.IconProfile
import com.example.personalization.model.IconStyleFamily
import com.example.personalization.model.InputStylePreset
import com.example.personalization.model.KeypadStylePreset
import com.example.personalization.model.LauncherIconStyle
import com.example.personalization.model.LayoutProfile
import com.example.personalization.model.MotionLevel
import com.example.personalization.model.NavigationLayoutStyle
import com.example.personalization.model.PersonalizationState
import com.example.personalization.model.ThemeDefinition
import com.example.personalization.model.ThemeSwitchMode
import com.example.personalization.model.TypographyProfile
import com.example.personalization.model.WidgetSize
import com.example.personalization.theme.ThemeRegistry
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.appearancePreferencesStore: DataStore<Preferences> by preferencesDataStore(
    name = "govinda_appearance_personalization",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)

class AppearanceDataStore(private val context: Context) {

    private object Keys {
        val SERIALIZED_STATE = stringPreferencesKey("serialized_personalization_state")
    }

    val stateFlow: Flow<PersonalizationState> = context.appearancePreferencesStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            val raw = prefs[Keys.SERIALIZED_STATE]
            if (raw.isNullOrBlank()) {
                defaultState()
            } else {
                runCatching { decodeState(raw) }.getOrElse { defaultState() }
            }
        }

    suspend fun saveState(state: PersonalizationState) {
        val encoded = encodeState(state)
        context.appearancePreferencesStore.edit { mutable ->
            mutable[Keys.SERIALIZED_STATE] = encoded
        }
    }

    suspend fun resetToDefaults() {
        saveState(defaultState())
    }

    companion object {
        fun defaultState(): PersonalizationState = PersonalizationState(
            activeProfile = ThemeRegistry.builtInProfiles.first(),
            savedProfiles = ThemeRegistry.builtInProfiles,
            customThemes = emptyList()
        )

        fun encodeState(state: PersonalizationState): String {
            return JSONObject().apply {
                put("activeProfile", encodeProfile(state.activeProfile))
                put("savedProfiles", JSONArray().apply {
                    state.savedProfiles.forEach { put(encodeProfile(it)) }
                })
                put("customThemes", JSONArray().apply {
                    state.customThemes.forEach { put(encodeTheme(it)) }
                })
            }.toString()
        }

        fun decodeState(raw: String): PersonalizationState {
            val root = JSONObject(raw)
            val active = root.optJSONObject("activeProfile")
                ?.let { runCatching { decodeProfile(it) }.getOrNull() }
                ?: ThemeRegistry.builtInProfiles.first()
            val savedArr = root.optJSONArray("savedProfiles")
            val saved = if (savedArr != null) {
                (0 until savedArr.length()).mapNotNull { idx ->
                    runCatching { decodeProfile(savedArr.getJSONObject(idx)) }.getOrNull()
                }
            } else {
                ThemeRegistry.builtInProfiles
            }
            val themesArr = root.optJSONArray("customThemes")
            val customThemes = if (themesArr != null) {
                (0 until themesArr.length()).mapNotNull { idx ->
                    runCatching { decodeTheme(themesArr.getJSONObject(idx)) }.getOrNull()
                }
            } else {
                emptyList()
            }
            return PersonalizationState(
                activeProfile = active,
                savedProfiles = saved.ifEmpty { ThemeRegistry.builtInProfiles },
                customThemes = customThemes
            )
        }

        private fun encodeProfile(p: AppearanceProfile): JSONObject = JSONObject().apply {
            put("id", p.id)
            put("name", p.name)
            put("isBuiltIn", p.isBuiltIn)
            put("themeId", p.themeId)
            put("darkThemeId", p.darkThemeId)
            put("themeSwitchMode", p.themeSwitchMode.name)
            put("scheduledDarkStartHour", p.scheduledDarkStartHour)
            put("scheduledDarkEndHour", p.scheduledDarkEndHour)
            put("customColors", JSONObject().apply {
                put("enabled", p.customColors.enabled)
                p.customColors.primaryArgb?.let { put("primary", it) }
                p.customColors.accentArgb?.let { put("accent", it) }
                p.customColors.backgroundArgb?.let { put("background", it) }
                p.customColors.surfaceArgb?.let { put("surface", it) }
                p.customColors.primaryTextArgb?.let { put("primaryText", it) }
                p.customColors.borderArgb?.let { put("border", it) }
            })
            put("typography", JSONObject().apply {
                put("headingFontId", p.typography.headingFontId)
                put("bodyFontId", p.typography.bodyFontId)
                put("equationFontId", p.typography.equationFontId)
                put("technicalFontId", p.typography.technicalFontId)
                put("fontSizeScale", p.typography.fontSizeScale.toDouble())
                put("fontWeightLevel", p.typography.fontWeightLevel.name)
                put("letterSpacingScale", p.typography.letterSpacingScale.toDouble())
                put("lineHeightScale", p.typography.lineHeightScale.toDouble())
                put("tabularNumerals", p.typography.tabularNumerals)
            })
            put("iconProfile", JSONObject().apply {
                put("family", p.iconProfile.family.name)
                put("sizeDp", p.iconProfile.sizeDp.toDouble())
                put("strokeWidthDp", p.iconProfile.strokeWidthDp.toDouble())
                p.iconProfile.customTintArgb?.let { put("customTintArgb", it) }
                put("navIconFamily", p.iconProfile.navIconFamily.name)
                put("categoryIconFamily", p.iconProfile.categoryIconFamily.name)
            })
            put("avatar", JSONObject().apply {
                put("seed", p.avatar.seed)
                put("style", p.avatar.style.name)
                put("shapeVariant", p.avatar.shapeVariant)
                put("backgroundColorArgb", p.avatar.backgroundColorArgb)
                put("accentColorArgb", p.avatar.accentColorArgb)
                put("accessoryIndex", p.avatar.accessoryIndex)
                put("borderStyle", p.avatar.borderStyle.name)
                put("badge", p.avatar.badge.name)
                put("monogram", p.avatar.monogram)
            })
            put("layout", JSONObject().apply {
                put("challengeLayout", p.layout.challengeLayout.name)
                put("dashboardLayout", p.layout.dashboardLayout.name)
                put("navigationLayout", p.layout.navigationLayout.name)
                put("density", p.layout.density.name)
                put("widgets", JSONArray().apply {
                    p.layout.widgets.forEach { w ->
                        put(JSONObject().apply {
                            put("id", w.id.name)
                            put("visible", w.visible)
                            put("size", w.size.name)
                        })
                    }
                })
            })
            put("components", JSONObject().apply {
                put("cardStyle", p.components.cardStyle.name)
                put("buttonStyle", p.components.buttonStyle.name)
                put("inputStyle", p.components.inputStyle.name)
                put("keypadStyle", p.components.keypadStyle.name)
            })
            put("background", p.background.name)
            put("effects", JSONObject().apply {
                put("elevationLevel", p.effects.elevationLevel.name)
                put("motionLevel", p.effects.motionLevel.name)
                put("soundEnabled", p.effects.soundEnabled)
                put("hapticsEnabled", p.effects.hapticsEnabled)
                put("highContrastMode", p.effects.highContrastMode)
                put("extraLargeTargets", p.effects.extraLargeTargets)
            })
            put("launcherIconStyle", p.launcherIconStyle.name)
        }

        private fun decodeProfile(obj: JSONObject): AppearanceProfile {
            val defaults = AppearanceProfile()
            val ccObj = obj.optJSONObject("customColors")
            val typoObj = obj.optJSONObject("typography")
            val iconObj = obj.optJSONObject("iconProfile")
            val avObj = obj.optJSONObject("avatar")
            val layObj = obj.optJSONObject("layout")
            val compObj = obj.optJSONObject("components")
            val effObj = obj.optJSONObject("effects")

            val widgetsArr = layObj?.optJSONArray("widgets")
            val widgets = if (widgetsArr != null && widgetsArr.length() > 0) {
                List(widgetsArr.length()) { idx ->
                    val wObj = widgetsArr.getJSONObject(idx)
                    DashboardWidgetConfig(
                        id = enumOr(wObj.optString("id"), DashboardWidgetId.DAILY_STREAK),
                        visible = wObj.optBoolean("visible", true),
                        size = enumOr(wObj.optString("size"), WidgetSize.SMALL)
                    )
                }
            } else {
                defaults.layout.widgets
            }

            return AppearanceProfile(
                id = obj.optString("id", defaults.id),
                name = obj.optString("name", defaults.name),
                isBuiltIn = obj.optBoolean("isBuiltIn", false),
                themeId = obj.optString("themeId", defaults.themeId),
                darkThemeId = obj.optString("darkThemeId", defaults.darkThemeId),
                themeSwitchMode = enumOr(obj.optString("themeSwitchMode"), defaults.themeSwitchMode),
                scheduledDarkStartHour = obj.optInt("scheduledDarkStartHour", defaults.scheduledDarkStartHour),
                scheduledDarkEndHour = obj.optInt("scheduledDarkEndHour", defaults.scheduledDarkEndHour),
                customColors = CustomColorOverrides(
                    enabled = ccObj?.optBoolean("enabled", false) ?: false,
                    primaryArgb = ccObj?.optLongNullable("primary"),
                    accentArgb = ccObj?.optLongNullable("accent"),
                    backgroundArgb = ccObj?.optLongNullable("background"),
                    surfaceArgb = ccObj?.optLongNullable("surface"),
                    primaryTextArgb = ccObj?.optLongNullable("primaryText"),
                    borderArgb = ccObj?.optLongNullable("border")
                ),
                typography = TypographyProfile(
                    headingFontId = typoObj?.optString("headingFontId", defaults.typography.headingFontId) ?: defaults.typography.headingFontId,
                    bodyFontId = typoObj?.optString("bodyFontId", defaults.typography.bodyFontId) ?: defaults.typography.bodyFontId,
                    equationFontId = typoObj?.optString("equationFontId", defaults.typography.equationFontId) ?: defaults.typography.equationFontId,
                    technicalFontId = typoObj?.optString("technicalFontId", defaults.typography.technicalFontId) ?: defaults.typography.technicalFontId,
                    fontSizeScale = typoObj?.optDouble("fontSizeScale", 1.0)?.toFloat() ?: 1.0f,
                    fontWeightLevel = enumOr(typoObj?.optString("fontWeightLevel"), defaults.typography.fontWeightLevel),
                    letterSpacingScale = typoObj?.optDouble("letterSpacingScale", 1.0)?.toFloat() ?: 1.0f,
                    lineHeightScale = typoObj?.optDouble("lineHeightScale", 1.0)?.toFloat() ?: 1.0f,
                    tabularNumerals = typoObj?.optBoolean("tabularNumerals", true) ?: true
                ),
                iconProfile = IconProfile(
                    family = enumOr(iconObj?.optString("family"), defaults.iconProfile.family),
                    sizeDp = iconObj?.optDouble("sizeDp", 22.0)?.toFloat() ?: 22f,
                    strokeWidthDp = iconObj?.optDouble("strokeWidthDp", 2.0)?.toFloat() ?: 2.0f,
                    customTintArgb = iconObj?.optLongNullable("customTintArgb"),
                    navIconFamily = enumOr(iconObj?.optString("navIconFamily"), defaults.iconProfile.navIconFamily),
                    categoryIconFamily = enumOr(iconObj?.optString("categoryIconFamily"), defaults.iconProfile.categoryIconFamily)
                ),
                avatar = AvatarProfile(
                    seed = avObj?.optInt("seed", defaults.avatar.seed) ?: defaults.avatar.seed,
                    style = enumOr(avObj?.optString("style"), defaults.avatar.style),
                    shapeVariant = avObj?.optInt("shapeVariant", 0) ?: 0,
                    backgroundColorArgb = avObj?.optLong("backgroundColorArgb", defaults.avatar.backgroundColorArgb) ?: defaults.avatar.backgroundColorArgb,
                    accentColorArgb = avObj?.optLong("accentColorArgb", defaults.avatar.accentColorArgb) ?: defaults.avatar.accentColorArgb,
                    accessoryIndex = avObj?.optInt("accessoryIndex", 0) ?: 0,
                    borderStyle = enumOr(avObj?.optString("borderStyle"), defaults.avatar.borderStyle),
                    badge = enumOr(avObj?.optString("badge"), defaults.avatar.badge),
                    monogram = avObj?.optString("monogram", "G") ?: "G"
                ),
                layout = LayoutProfile(
                    challengeLayout = enumOr(layObj?.optString("challengeLayout"), defaults.layout.challengeLayout),
                    dashboardLayout = enumOr(layObj?.optString("dashboardLayout"), defaults.layout.dashboardLayout),
                    navigationLayout = enumOr(layObj?.optString("navigationLayout"), defaults.layout.navigationLayout),
                    density = enumOr(layObj?.optString("density"), defaults.layout.density),
                    widgets = widgets
                ),
                components = ComponentStyle(
                    cardStyle = enumOr(compObj?.optString("cardStyle"), defaults.components.cardStyle),
                    buttonStyle = enumOr(compObj?.optString("buttonStyle"), defaults.components.buttonStyle),
                    inputStyle = enumOr(compObj?.optString("inputStyle"), defaults.components.inputStyle),
                    keypadStyle = enumOr(compObj?.optString("keypadStyle"), defaults.components.keypadStyle)
                ),
                background = enumOr(obj.optString("background"), defaults.background),
                effects = EffectSettings(
                    elevationLevel = enumOr(effObj?.optString("elevationLevel"), defaults.effects.elevationLevel),
                    motionLevel = enumOr(effObj?.optString("motionLevel"), defaults.effects.motionLevel),
                    soundEnabled = effObj?.optBoolean("soundEnabled", false) ?: false,
                    hapticsEnabled = effObj?.optBoolean("hapticsEnabled", true) ?: true,
                    highContrastMode = effObj?.optBoolean("highContrastMode", false) ?: false,
                    extraLargeTargets = effObj?.optBoolean("extraLargeTargets", false) ?: false
                ),
                launcherIconStyle = enumOr(obj.optString("launcherIconStyle"), defaults.launcherIconStyle)
            )
        }

        private fun encodeTheme(t: ThemeDefinition): JSONObject = JSONObject().apply {
            put("id", t.id)
            put("name", t.name)
            put("isDark", t.isDark)
            put("isCustom", t.isCustom)
            put("backgroundArgb", t.backgroundArgb)
            put("surfaceArgb", t.surfaceArgb)
            put("elevatedSurfaceArgb", t.elevatedSurfaceArgb)
            put("primaryArgb", t.primaryArgb)
            put("secondaryArgb", t.secondaryArgb)
            put("accentArgb", t.accentArgb)
            put("onPrimaryArgb", t.onPrimaryArgb)
            put("primaryTextArgb", t.primaryTextArgb)
            put("secondaryTextArgb", t.secondaryTextArgb)
            put("mutedTextArgb", t.mutedTextArgb)
            put("borderArgb", t.borderArgb)
            put("dividerArgb", t.dividerArgb)
            put("successArgb", t.successArgb)
            put("warningArgb", t.warningArgb)
            put("errorArgb", t.errorArgb)
            put("selectedStateArgb", t.selectedStateArgb)
            put("disabledStateArgb", t.disabledStateArgb)
        }

        private fun decodeTheme(obj: JSONObject): ThemeDefinition = ThemeDefinition(
            id = obj.getString("id"),
            name = obj.getString("name"),
            isDark = obj.getBoolean("isDark"),
            isCustom = obj.optBoolean("isCustom", true),
            backgroundArgb = obj.getLong("backgroundArgb"),
            surfaceArgb = obj.getLong("surfaceArgb"),
            elevatedSurfaceArgb = obj.getLong("elevatedSurfaceArgb"),
            primaryArgb = obj.getLong("primaryArgb"),
            secondaryArgb = obj.getLong("secondaryArgb"),
            accentArgb = obj.getLong("accentArgb"),
            onPrimaryArgb = obj.getLong("onPrimaryArgb"),
            primaryTextArgb = obj.getLong("primaryTextArgb"),
            secondaryTextArgb = obj.getLong("secondaryTextArgb"),
            mutedTextArgb = obj.getLong("mutedTextArgb"),
            borderArgb = obj.getLong("borderArgb"),
            dividerArgb = obj.getLong("dividerArgb"),
            successArgb = obj.getLong("successArgb"),
            warningArgb = obj.getLong("warningArgb"),
            errorArgb = obj.getLong("errorArgb"),
            selectedStateArgb = obj.getLong("selectedStateArgb"),
            disabledStateArgb = obj.getLong("disabledStateArgb")
        )

        private inline fun <reified E : Enum<E>> enumOr(raw: String?, fallback: E): E {
            if (raw.isNullOrBlank()) return fallback
            return runCatching { enumValueOf<E>(raw) }.getOrDefault(fallback)
        }

        private fun JSONObject.optLongNullable(key: String): Long? {
            return if (has(key) && !isNull(key)) optLong(key) else null
        }
    }
}
