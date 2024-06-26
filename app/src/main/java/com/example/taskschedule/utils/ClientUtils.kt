package com.example.taskschedule.utils

import io.ktor.client.plugins.auth.providers.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


    /************************************************************************
     * Excepciones
     *************************************************************************/

class AuthenticationException : Exception()
class UserExistsException : Exception()


    /************************************************************************
     * Json del token
     *************************************************************************/
@Serializable
data class TokenInfo(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String,
)


    /************************************************************************
     * Lista donde se guarda el token
     *************************************************************************/
internal val bearerTokenStorage = mutableListOf<BearerTokens>()
