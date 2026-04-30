package com.codequest.domain.usecase

import com.codequest.domain.model.Language
import com.codequest.domain.repository.LanguageRepository

class GetLanguagesUseCase(private val repository: LanguageRepository) {
    suspend operator fun invoke(): Result<List<Language>> {
        return repository.getAvailableLanguages()
    }
}
