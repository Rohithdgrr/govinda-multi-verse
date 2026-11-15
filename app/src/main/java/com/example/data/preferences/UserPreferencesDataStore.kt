package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AdaptivePace
import com.example.domain.model.ChallengeMode
import com.example.domain.model.Difficulty
import com.example.domain.model.InputMode
import com.example.domain.model.MathCategory
import com.example.domain.model.TopicMixMode
import com.example.domain.model.UserPreferences
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.govindaPreferencesStore: DataStore<Preferences> by preferencesDataStore(
    name = "govinda_user_preferences",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)

class UserPreferencesDataStore(private val context: Context) {

    private object Keys {
        val DEFAULT_INPUT_MODE = stringPreferencesKey("default_input_mode")
        val DEFAULT_CHALLENGE_MODE = stringPreferencesKey("default_challenge_mode")
        val SESSION_LENGTH = intPreferencesKey("session_length")
        val ALLOW_NEGATIVE_SUBTRACTION = booleanPreferencesKey("allow_negative_subtraction")
        val MANUAL_DIFFICULTY = stringPreferencesKey("manual_difficulty")
        val MIN_DIFFICULTY = stringPreferencesKey("min_difficulty")
        val MAX_DIFFICULTY = stringPreferencesKey("max_difficulty")
        val ENABLED_CATEGORIES = stringSetPreferencesKey("enabled_categories")
        val QUIZ_LOCK_ENABLED = booleanPreferencesKey("quiz_lock_enabled")
        val QUIZ_LOCKED_PACKAGES = stringSetPreferencesKey("quiz_locked_packages")
        val HAS_ACCEPTED_PERMISSIONS = booleanPreferencesKey("has_accepted_permissions")
        val HAS_COMPLETED_SURVEY = booleanPreferencesKey("has_completed_survey")
        val LEARNER_TRACK_ID = stringPreferencesKey("learner_track_id")
        val LEARNER_TRACK_LABEL = stringPreferencesKey("learner_track_label")
        val SURVEY_FOCUS_TAG = stringPreferencesKey("survey_focus_tag")
        val START_SIMPLE_ENABLED = booleanPreferencesKey("start_simple_enabled")
        val WARM_UP_QUESTION_COUNT = intPreferencesKey("warm_up_question_count")
        val ADAPTIVE_PACE = stringPreferencesKey("adaptive_pace")
        val PROGRESSIVE_STYLE_ENABLED = booleanPreferencesKey("progressive_style_enabled")
        val SPEED_WEIGHT_ENABLED = booleanPreferencesKey("speed_weight_enabled")
        val TOPIC_MIX_MODE = stringPreferencesKey("topic_mix_mode")
        val DEVELOPER_MODE_ENABLED = booleanPreferencesKey("developer_mode_enabled")
    }

    val preferencesFlow: Flow<UserPreferences> = context.govindaPreferencesStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs -> mapFrom(prefs) }

    suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        context.govindaPreferencesStore.edit { mutable ->
            val current = mapFrom(mutable)
            val updated = transform(current)
            mutable[Keys.DEFAULT_INPUT_MODE] = updated.defaultInputMode.name
            mutable[Keys.DEFAULT_CHALLENGE_MODE] = updated.defaultChallengeMode.name
            mutable[Keys.SESSION_LENGTH] = updated.sessionLength
            mutable[Keys.ALLOW_NEGATIVE_SUBTRACTION] = updated.allowNegativeSubtraction
            if (updated.manualDifficultyOverride != null) {
                mutable[Keys.MANUAL_DIFFICULTY] = updated.manualDifficultyOverride.name
            } else {
                mutable.remove(Keys.MANUAL_DIFFICULTY)
            }
            mutable[Keys.MIN_DIFFICULTY] = updated.minDifficulty.name
            mutable[Keys.MAX_DIFFICULTY] = updated.maxDifficulty.name
            mutable[Keys.ENABLED_CATEGORIES] = updated.enabledCategories.map { it.name }.toSet()
            mutable[Keys.QUIZ_LOCK_ENABLED] = updated.quizLockEnabled
            mutable[Keys.QUIZ_LOCKED_PACKAGES] = updated.quizLockedPackages
            mutable[Keys.HAS_ACCEPTED_PERMISSIONS] = updated.hasAcceptedPermissions
            mutable[Keys.HAS_COMPLETED_SURVEY] = updated.hasCompletedSurvey
            mutable[Keys.LEARNER_TRACK_ID] = updated.learnerTrackId
            mutable[Keys.LEARNER_TRACK_LABEL] = updated.learnerTrackLabel
            mutable[Keys.SURVEY_FOCUS_TAG] = updated.surveyFocusTag
            mutable[Keys.START_SIMPLE_ENABLED] = updated.startSimpleEnabled
            mutable[Keys.WARM_UP_QUESTION_COUNT] = updated.warmUpQuestionCount.coerceIn(1, 15)
            mutable[Keys.ADAPTIVE_PACE] = updated.adaptivePace.name
            mutable[Keys.PROGRESSIVE_STYLE_ENABLED] = updated.progressiveStyleEnabled
            mutable[Keys.SPEED_WEIGHT_ENABLED] = updated.speedWeightEnabled
            mutable[Keys.TOPIC_MIX_MODE] = updated.topicMixMode.name
            mutable[Keys.DEVELOPER_MODE_ENABLED] = updated.developerModeEnabled
        }
    }

    suspend fun resetDefaults() {
        context.govindaPreferencesStore.edit { it.clear() }
    }

    private fun mapFrom(prefs: Preferences): UserPreferences {
        val defaultInput = prefs[Keys.DEFAULT_INPUT_MODE]
            ?.let { runCatching { InputMode.valueOf(it) }.getOrNull() }
            ?: InputMode.KEYPAD
        val defaultChallenge = prefs[Keys.DEFAULT_CHALLENGE_MODE]
            ?.let { runCatching { ChallengeMode.valueOf(it) }.getOrNull() }
            ?: ChallengeMode.CONTINUOUS
        val sessionLength = (prefs[Keys.SESSION_LENGTH] ?: 10).coerceIn(5, 30)
        val allowNegative = prefs[Keys.ALLOW_NEGATIVE_SUBTRACTION] ?: false
        val manualOverride = prefs[Keys.MANUAL_DIFFICULTY]
            ?.takeIf { it.isNotEmpty() }
            ?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() }
        val minDifficulty = prefs[Keys.MIN_DIFFICULTY]
            ?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() }
            ?: Difficulty.BEGINNER
        val maxDifficulty = prefs[Keys.MAX_DIFFICULTY]
            ?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() }
            ?: Difficulty.EXPERT
        val enabledSet = prefs[Keys.ENABLED_CATEGORIES]
            ?.mapNotNull { name -> runCatching { MathCategory.valueOf(name) }.getOrNull() }
            ?.toSet()
            ?.takeIf { it.isNotEmpty() }
            ?: MathCategory.defaultBalancedSet
        val quizLockEnabled = prefs[Keys.QUIZ_LOCK_ENABLED] ?: false
        val quizLockedPackages = prefs[Keys.QUIZ_LOCKED_PACKAGES] ?: emptySet()
        val hasAcceptedPermissions = prefs[Keys.HAS_ACCEPTED_PERMISSIONS] ?: false
        val hasCompletedSurvey = prefs[Keys.HAS_COMPLETED_SURVEY] ?: false
        val learnerTrackId = prefs[Keys.LEARNER_TRACK_ID] ?: "DEFAULT"
        val learnerTrackLabel = prefs[Keys.LEARNER_TRACK_LABEL] ?: "By Default (Adaptive)"
        val surveyFocusTag = prefs[Keys.SURVEY_FOCUS_TAG] ?: ""

        val startSimpleEnabled = prefs[Keys.START_SIMPLE_ENABLED] ?: true
        val warmUpQuestionCount = (prefs[Keys.WARM_UP_QUESTION_COUNT] ?: 5).coerceIn(1, 15)
        val adaptivePace = prefs[Keys.ADAPTIVE_PACE]
            ?.let { runCatching { AdaptivePace.valueOf(it) }.getOrNull() }
            ?: AdaptivePace.BALANCED
        val progressiveStyleEnabled = prefs[Keys.PROGRESSIVE_STYLE_ENABLED] ?: true
        val speedWeightEnabled = prefs[Keys.SPEED_WEIGHT_ENABLED] ?: true
        val topicMixMode = prefs[Keys.TOPIC_MIX_MODE]
            ?.let { runCatching { TopicMixMode.valueOf(it) }.getOrNull() }
            ?: TopicMixMode.PROGRESSIVE_UNLOCK
        val developerModeEnabled = prefs[Keys.DEVELOPER_MODE_ENABLED] ?: false

        return UserPreferences(
            defaultInputMode = defaultInput,
            defaultChallengeMode = defaultChallenge,
            sessionLength = sessionLength,
            allowNegativeSubtraction = allowNegative,
            manualDifficultyOverride = manualOverride,
            minDifficulty = minDifficulty,
            maxDifficulty = maxDifficulty,
            enabledCategories = enabledSet,
            quizLockEnabled = quizLockEnabled,
            quizLockedPackages = quizLockedPackages,
            hasAcceptedPermissions = hasAcceptedPermissions,
            hasCompletedSurvey = hasCompletedSurvey,
            learnerTrackId = learnerTrackId,
            learnerTrackLabel = learnerTrackLabel,
            surveyFocusTag = surveyFocusTag,
            startSimpleEnabled = startSimpleEnabled,
            warmUpQuestionCount = warmUpQuestionCount,
            adaptivePace = adaptivePace,
            progressiveStyleEnabled = progressiveStyleEnabled,
            speedWeightEnabled = speedWeightEnabled,
            topicMixMode = topicMixMode,
            developerModeEnabled = developerModeEnabled
        )
    }
}
