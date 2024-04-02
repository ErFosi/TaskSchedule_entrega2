package com.example.taskschedule.utils

import io.ktor.client.plugins.auth.providers.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/*******************************************************************************
 ****                               Exceptions                              ****
 *******************************************************************************/

class AuthenticationException : Exception()
class UserExistsException : Exception()


/*******************************************************************************
 ****                         Response Data Classes                         ****
 *******************************************************************************/

/**
 * Data class that represents server response when an [accessToken] is request.
 */
@Serializable
data class TokenInfo(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String,
)


/*******************************************************************************
 ****                          Bearer Token Storage                         ****
 *******************************************************************************/

/**
 * [MutableList] to save retrieves [BearerTokens]
 */
internal val bearerTokenStorage = mutableListOf<BearerTokens>()