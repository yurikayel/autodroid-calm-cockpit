package com.example.autodroid.feature.weather.data.repository

import com.example.autodroid.feature.weather.domain.model.AppError
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

internal fun Exception.toAppError(): AppError {
    if (this is CancellationException) throw this
    return when (this) {
        is java.net.SocketTimeoutException -> AppError.NetworkError.Timeout
        is javax.net.ssl.SSLException -> AppError.NetworkError.Unknown(this)
        is java.net.UnknownHostException,
        is java.net.ConnectException,
        is java.net.NoRouteToHostException -> AppError.NetworkError.NoConnectivity
        is IOException -> AppError.NetworkError.Unknown(this)
        is HttpException -> {
            when (code()) {
                404 -> AppError.ApiError.NotFound
                429 -> AppError.ApiError.RateLimitExceeded
                408 -> AppError.NetworkError.Timeout
                else -> AppError.NetworkError.ServerError(code())
            }
        }
        else -> AppError.Unknown(this)
    }
}
