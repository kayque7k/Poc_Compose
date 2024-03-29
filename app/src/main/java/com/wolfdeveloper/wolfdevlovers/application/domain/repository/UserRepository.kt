package com.wolfdeveloper.wolfdevlovers.application.domain.repository

import android.net.Uri
import com.wolfdeveloper.wolfdevlovers.application.domain.model.User

interface UserRepository {

    suspend fun get(code: String): User?

    suspend fun insert(insert: User): User

    suspend fun profile(uri: Uri, user: User): User

    suspend fun background(uri: Uri, user: User): User
}