package com.example.di

import android.content.Context
import com.example.data.local.database.GovindaDatabase
import com.example.data.preferences.UserPreferencesDataStore
import com.example.data.repository.RoomMathLearningRepository
import com.example.data.repository.RoomQuestionRepository
import com.example.domain.adaptive.AdaptiveController
import com.example.domain.evaluator.AnswerEvaluator
import com.example.domain.generator.GeneratorRegistry
import com.example.domain.repository.MathLearningRepository
import com.example.domain.repository.QuestionRepository
import com.example.domain.selector.TopicSelector
import com.example.domain.usecase.ExportLearningDataUseCase
import com.example.domain.usecase.GenerateNextQuestionUseCase
import com.example.domain.usecase.ResetLearningProfileUseCase
import com.example.domain.usecase.SubmitAnswerUseCase
import com.example.feature.chat.ChatRepository
import com.example.feature.chat.CloudChatRepository
import com.example.feature.notes.NotesStudioRepository
import com.example.personalization.repository.PersonalizationRepository
import com.example.personalization.storage.AppearanceDataStore

class AppContainer(private val appContext: Context) {
    private val database: GovindaDatabase by lazy {
        GovindaDatabase.build(appContext)
    }

    private val preferencesDataStore: UserPreferencesDataStore by lazy {
        UserPreferencesDataStore(appContext)
    }

    private val appearanceDataStore: AppearanceDataStore by lazy {
        AppearanceDataStore(appContext)
    }

    val personalizationRepository: PersonalizationRepository by lazy {
        PersonalizationRepository(appearanceDataStore)
    }

    val repository: MathLearningRepository by lazy {
        RoomMathLearningRepository(database, preferencesDataStore)
    }

    val questionRepository: QuestionRepository by lazy {
        RoomQuestionRepository(database)
    }

    val notesStudioRepository: NotesStudioRepository by lazy {
        NotesStudioRepository(database)
    }

    val chatRepository: ChatRepository by lazy {
        ChatRepository(database)
    }

    val cloudChatRepository: CloudChatRepository? by lazy {
        runCatching { CloudChatRepository(appContext.applicationContext) }.getOrNull()
    }

    val generatorRegistry: GeneratorRegistry by lazy {
        GeneratorRegistry.default()
    }

    val answerEvaluator: AnswerEvaluator by lazy {
        AnswerEvaluator()
    }

    val adaptiveController: AdaptiveController by lazy {
        AdaptiveController(windowSize = 10)
    }

    val topicSelector: TopicSelector by lazy {
        TopicSelector()
    }

    val generateNextQuestionUseCase: GenerateNextQuestionUseCase by lazy {
        GenerateNextQuestionUseCase(
            repository = repository,
            registry = generatorRegistry,
            topicSelector = topicSelector,
            adaptiveController = adaptiveController
        )
    }

    val submitAnswerUseCase: SubmitAnswerUseCase by lazy {
        SubmitAnswerUseCase(
            repository = repository,
            evaluator = answerEvaluator,
            adaptiveController = adaptiveController
        )
    }

    val exportLearningDataUseCase: ExportLearningDataUseCase by lazy {
        ExportLearningDataUseCase(repository)
    }

    val resetLearningProfileUseCase: ResetLearningProfileUseCase by lazy {
        ResetLearningProfileUseCase(repository)
    }
}
