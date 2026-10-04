package com.rhinepereira.saints.data.model

import retrofit2.HttpException
import java.io.IOException

sealed class UiError {
    object Network : UiError()
    object NotFound : UiError()
    data class Unknown(val message: String?) : UiError()

    companion object {
        fun from(e: Throwable): UiError = when (e) {
            is IOException -> Network
            is HttpException -> if (e.code() == 404) NotFound else Unknown(e.message())
            else -> Unknown(e.message)
        }
    }
}
