package com.example.feature.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.adaptive.AdaptiveController
import com.example.domain.evaluator.AnswerEvaluator
import com.example.domain.generator.GeneratorConfig
import com.example.domain.generator.GeneratorRegistry
import com.example.domain.model.Difficulty
import com.example.domain.model.MathCategory
import com.example.domain.model.MathQuestion
import com.example.domain.model.QuestionAttempt
import com.example.domain.model.SkillProfile
import com.example.domain.model.UserPreferences
import com.example.domain.repository.MathLearningRepository
import com.example.domain.usecase.GenerateNextQuestionUseCase
import com.example.domain.usecase.SubmissionOutcome
import com.example.domain.usecase.SubmitAnswerUseCase
import com.example.lock.QuizLockSessionHolder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

data class ChallengeUiState(
    val currentQuestion: MathQuestion? = null,
    val isLoading: Boolean = true,
    val userInput: String = "",
    val submissionOutcome: SubmissionOutcome? = null,
    val isExplanationExpanded: Boolean = false,
    val wrongCooldownSecondsRemaining: Int = 0,
    val wrongCooldownTotalSeconds: Int = DEFAULT_WRONG_COOLDOWN_SECONDS,
    val wrongAttemptCount: Int = 0,
    val lockedPackage: String? = null,
    val lockedAppName: String? = null,
    val unlockedPackageToLaunch: String? = null,
    val preferences: UserPreferences = UserPreferences()
) {
    val isCooldownActive: Boolean
        get() = wrongCooldownSecondsRemaining > 0

    val canSubmit: Boolean
        get() = submissionOutcome == null &&
            wrongCooldownSecondsRemaining == 0 &&
            currentQuestion != null &&
            userInput.isNotBlank() &&
            userInput != "-" &&
            !userInput.endsWith("/")

    companion object {
        const val DEFAULT_WRONG_COOLDOWN_SECONDS = 20
    }
}

class ChallengeViewModel(
    private val repository: MathLearningRepository,
    private val generateNextQuestion: GenerateNextQuestionUseCase,
    private val submitAnswer: SubmitAnswerUseCase,
    private val cooldownDurationSeconds: Int = ChallengeUiState.DEFAULT_WRONG_COOLDOWN_SECONDS,
    private val cooldownTickMs: Long = 1000L
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeUiState(wrongCooldownTotalSeconds = cooldownDurationSeconds))
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    private var questionStartedAtMs: Long = System.currentTimeMillis()
    private var lastGeneratedCategory: MathCategory? = null
    private var initialLoadTriggered = false
    private var cooldownJob: Job? = null
    private var loadJob: Job? = null
    private var submitJob: Job? = null

    init {
        observePreferences()
        loadJob = viewModelScope.launch {
            if (!initialLoadTriggered) {
                initialLoadTriggered = true
                val prefs = runCatching {
                    withTimeoutOrNull(4_000L) { repository.observePreferences().first() }
                }.getOrNull() ?: UserPreferences()
                _uiState.update { it.copy(preferences = prefs) }
                loadNextQuestionInternal()
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            repository.observePreferences().collect { prefs ->
                _uiState.update { it.copy(preferences = prefs) }
            }
        }
    }

    fun onFreshAppOpen(lockedPackage: String?, lockedAppName: String?) {
        initialLoadTriggered = true
        cancelCooldown()
        loadJob?.cancel()
        _uiState.update {
            it.copy(
                lockedPackage = lockedPackage,
                lockedAppName = lockedAppName,
                unlockedPackageToLaunch = null,
                wrongCooldownSecondsRemaining = 0,
                wrongAttemptCount = 0
            )
        }
        loadJob = viewModelScope.launch {
            val prefs = runCatching {
                withTimeoutOrNull(4_000L) { repository.observePreferences().first() }
            }.getOrNull() ?: _uiState.value.preferences
            _uiState.update { it.copy(preferences = prefs) }
            loadNextQuestionInternal()
        }
    }

    fun onReturnedFromBackground() {
        val state = _uiState.value
        if (state.lockedPackage == null &&
            state.submissionOutcome?.evaluation?.isCorrect == true &&
            !state.isCooldownActive
        ) {
            viewModelScope.launch {
                loadNextQuestionInternal()
            }
        }
    }

    fun reloadQuestionForUpdatedProfile(updatedPrefs: UserPreferences) {
        cancelCooldown()
        loadJob?.cancel()
        _uiState.update { it.copy(preferences = updatedPrefs) }
        loadJob = viewModelScope.launch {
            loadNextQuestionInternal()
        }
    }

    fun consumeUnlockedPackageLaunch() {
        _uiState.update {
            it.copy(
                unlockedPackageToLaunch = null,
                lockedPackage = null,
                lockedAppName = null,
                wrongAttemptCount = 0
            )
        }
    }

    fun clearLockGate() {
        cancelCooldown()
        _uiState.update {
            it.copy(
                unlockedPackageToLaunch = null,
                lockedPackage = null,
                lockedAppName = null,
                wrongCooldownSecondsRemaining = 0,
                wrongAttemptCount = 0
            )
        }
    }

    fun onKeypadSymbol(symbol: String) {
        val state = _uiState.value
        if (state.submissionOutcome != null || state.isCooldownActive) return
        val current = state.userInput
        if (current.length >= 14) return

        val next = when (symbol) {
            "-" -> {
                if (current.isEmpty()) "-"
                else if (current.startsWith("-")) current.drop(1)
                else current
            }
            "." -> {
                if (current.contains(".") || current.contains("/")) current
                else if (current.isEmpty() || current == "-") "${current}0."
                else "$current."
            }
            "/" -> {
                if (current.isEmpty() || current == "-" || current.contains("/") || current.contains(".")) current
                else "$current/"
            }
            else -> current + symbol
        }
        _uiState.update { it.copy(userInput = next) }
    }

    fun onBackspace() {
        val state = _uiState.value
        if (state.submissionOutcome != null || state.isCooldownActive) return
        _uiState.update { s ->
            if (s.userInput.isEmpty()) s
            else s.copy(userInput = s.userInput.dropLast(1))
        }
    }

    fun onClearInput() {
        val state = _uiState.value
        if (state.submissionOutcome != null || state.isCooldownActive) return
        _uiState.update { it.copy(userInput = "") }
    }

    fun onSelectMcqOption(optionText: String) {
        val state = _uiState.value
        if (state.submissionOutcome != null || state.isCooldownActive) return
        _uiState.update { it.copy(userInput = optionText) }
    }

    fun onSubmitAnswer() {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        if (state.submissionOutcome != null || !state.canSubmit || state.isCooldownActive || submitJob?.isActive == true) return

        val rawAnswer = state.userInput
        val responseTimeMs = (System.currentTimeMillis() - questionStartedAtMs).coerceAtLeast(300L)

        submitJob = viewModelScope.launch {
            val outcome = runCatching {
                withTimeoutOrNull(4_000L) {
                    submitAnswer(
                        question = question,
                        rawInput = rawAnswer,
                        responseTimeMs = responseTimeMs,
                        preferences = state.preferences
                    )
                }
            }.getOrNull() ?: fallbackEvaluateLocally(question, rawAnswer, responseTimeMs, state.preferences)

            val lockedPkg = state.lockedPackage
            if (lockedPkg != null && outcome.evaluation.isCorrect) {
                cancelCooldown()
                QuizLockSessionHolder.tracker.markUnlocked(lockedPkg)
                _uiState.update {
                    it.copy(
                        submissionOutcome = outcome,
                        isExplanationExpanded = false,
                        wrongCooldownSecondsRemaining = 0,
                        wrongAttemptCount = 0,
                        unlockedPackageToLaunch = lockedPkg
                    )
                }
            } else if (!outcome.evaluation.isCorrect) {
                val nextAttempts = state.wrongAttemptCount + 1
                if (cooldownDurationSeconds <= 0) {
                    _uiState.update {
                        it.copy(
                            submissionOutcome = null,
                            userInput = "",
                            isExplanationExpanded = false,
                            wrongAttemptCount = nextAttempts,
                            wrongCooldownSecondsRemaining = 0,
                            wrongCooldownTotalSeconds = 0
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            submissionOutcome = outcome,
                            isExplanationExpanded = false,
                            wrongAttemptCount = nextAttempts,
                            wrongCooldownSecondsRemaining = cooldownDurationSeconds,
                            wrongCooldownTotalSeconds = cooldownDurationSeconds
                        )
                    }
                    startWrongCooldown()
                }
            } else {
                cancelCooldown()
                _uiState.update {
                    it.copy(
                        submissionOutcome = outcome,
                        isExplanationExpanded = false,
                        wrongCooldownSecondsRemaining = 0,
                        wrongAttemptCount = 0
                    )
                }
            }
        }
    }

    fun onNextQuestion() {
        val state = _uiState.value
        if (state.isCooldownActive || state.submissionOutcome?.evaluation?.isCorrect != true) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            loadNextQuestionInternal()
        }
    }

    fun onToggleExplanation() {
        if (_uiState.value.submissionOutcome?.evaluation?.isCorrect != true) return
        _uiState.update { it.copy(isExplanationExpanded = !it.isExplanationExpanded) }
    }

    private fun startWrongCooldown() {
        cancelCooldown()
        if (cooldownDurationSeconds <= 0) return
        cooldownJob = viewModelScope.launch {
            var remaining = cooldownDurationSeconds
            while (remaining > 0) {
                delay(cooldownTickMs)
                remaining -= 1
                if (remaining > 0) {
                    _uiState.update { it.copy(wrongCooldownSecondsRemaining = remaining) }
                } else {
                    // Unlock same question for retry immediately when 20s finishes
                    _uiState.update {
                        it.copy(
                            wrongCooldownSecondsRemaining = 0,
                            submissionOutcome = null,
                            userInput = "",
                            isExplanationExpanded = false
                        )
                    }
                }
            }
        }
    }

    private fun cancelCooldown() {
        cooldownJob?.cancel()
        cooldownJob = null
    }

    private suspend fun loadNextQuestionInternal() {
        cancelCooldown()
        _uiState.update {
            it.copy(
                isLoading = true,
                userInput = "",
                submissionOutcome = null,
                isExplanationExpanded = false,
                wrongCooldownSecondsRemaining = 0,
                wrongAttemptCount = 0
            )
        }

        val state = _uiState.value
        val question = runCatching {
            withTimeoutOrNull(4_000L) {
                generateNextQuestion(
                    preferences = state.preferences,
                    forcedCategory = null,
                    lastCategory = lastGeneratedCategory
                )
            }
        }.getOrNull() ?: run {
            val fallbackCategory = state.preferences.enabledCategories.firstOrNull() ?: MathCategory.ADDITION
            GeneratorRegistry.default().generatorFor(fallbackCategory).generate(
                difficulty = state.preferences.minDifficulty,
                config = GeneratorConfig(
                    allowNegativeSubtraction = state.preferences.allowNegativeSubtraction,
                    focusTag = state.preferences.surveyFocusTag
                )
            )
        }
        lastGeneratedCategory = question.category
        questionStartedAtMs = System.currentTimeMillis()

        _uiState.update {
            it.copy(
                currentQuestion = question,
                isLoading = false
            )
        }
    }

    // Ensures submit flow completes even if local DB write hits a transient SQLite lock/timeout
    private fun fallbackEvaluateLocally(
        question: MathQuestion,
        rawAnswer: String,
        responseTimeMs: Long,
        preferences: UserPreferences
    ): SubmissionOutcome {
        val evaluation = AnswerEvaluator().evaluate(question, rawAnswer)
        val attempt = QuestionAttempt(
            id = UUID.randomUUID().toString(),
            questionId = question.id,
            category = question.category,
            difficulty = question.difficulty,
            prompt = question.prompt,
            expectedAnswer = question.answer,
            submittedAnswer = evaluation.normalizedSubmitted.ifEmpty { rawAnswer.trim() },
            isCorrect = evaluation.isCorrect,
            responseTimeMs = responseTimeMs,
            attemptedAtEpochMs = System.currentTimeMillis()
        )
        val adaptiveResult = AdaptiveController().updateProfile(
            currentProfile = SkillProfile.initial(question.category),
            newAttempt = attempt,
            recentAttemptsForCategory = emptyList(),
            expectedTimeSeconds = question.expectedTimeSeconds,
            preferences = preferences
        )
        return SubmissionOutcome(
            evaluation = evaluation,
            adaptiveResult = adaptiveResult,
            xpEarned = if (evaluation.isCorrect) question.difficulty.baseXp else 0
        )
    }

    class Factory(
        private val repository: MathLearningRepository,
        private val generateNextQuestion: GenerateNextQuestionUseCase,
        private val submitAnswer: SubmitAnswerUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChallengeViewModel(repository, generateNextQuestion, submitAnswer) as T
        }
    }
}
