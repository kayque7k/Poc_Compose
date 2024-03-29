package com.wolfdeveloper.wolfdevlovers.commons.extensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> safeRunDispatcher(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    block: suspend CoroutineScope.() -> (T)
) = withContext(dispatcher) {
    return@withContext try {
        val result = block()
        Result.Success(result)
    } catch (ex: Exception) {
        Result.Failure(ex)
    }
}

sealed class Result<out T> {
    class Success<T>(val data: T) : Result<T>()
    class Failure(val err: Exception) : Result<Nothing>()
}
