package com.codequest.di

import com.codequest.data.local.DataStoreManager
import com.codequest.data.remote.supabaseClient
import com.codequest.data.repository.*
import com.codequest.domain.repository.*
import com.codequest.domain.usecase.*
import com.codequest.presentation.viewmodel.*
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val networkModule = module {
    single { supabaseClient }
}

val dataModule = module {
    single { DataStoreManager(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<LanguageRepository> { LanguageRepositoryImpl(get(), get()) }
    single<UserStatsRepository> { UserStatsRepositoryImpl(get(), get()) }
    single<AttemptRepository> { AttemptRepositoryImpl(get()) }
}

val domainModule = module {
    factory { GetLanguagesUseCase(get()) }
    factory { SubmitLessonUseCase(get(), get()) }
}

val viewModelModule = module {
    factory { AuthViewModel(get()) }
    factory { HomeViewModel(get(), get(), get()) }
    factory { LanguageViewModel(get(), get(), get()) }
    factory { QuizViewModel(get(), get(), get()) }
    factory { LeaderboardViewModel(get()) }
    factory { ProfileViewModel(get(), get()) }
}

expect val platformModule: Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(
        networkModule,
        dataModule,
        domainModule,
        viewModelModule,
        platformModule
    )
}
