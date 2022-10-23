package com.teste.poc.application.usecase

import android.net.Uri
import com.teste.poc.application.domain.model.User
import com.teste.poc.application.domain.repository.UserRepository
import com.teste.poc.commons.extensions.safeRunDispatcher

class DashboardProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend fun execute(uri: Uri,code: String) = safeRunDispatcher {
        userRepository.profile(
            uri = uri,
            user = User().apply {
                this.code = code
            }
        )
    }
}