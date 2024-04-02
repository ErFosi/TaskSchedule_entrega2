package com.example.taskschedule.utils

import com.example.taskschedule.data.UsuarioCred
import io.ktor.client.HttpClient
import io.ktor.client.*
import io.ktor.client.call.body
import javax.inject.Inject
import javax.inject.Singleton
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import com.example.taskschedule.utils.*
import com.example.taskschedule.utils.TokenInfo



@Singleton
class AuthWebClient @Inject constructor() {
    //CIO permite corrutinas lo que permite asincronia con kotlin
    private val clienteHttp = HttpClient(CIO) {
        expectSuccess = true


        install(ContentNegotiation) { json() }

        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, _ ->
                when {
                    exception is ClientRequestException && exception.response.status == HttpStatusCode.Unauthorized -> throw AuthenticationException()
                    exception is ClientRequestException && exception.response.status == HttpStatusCode.Conflict -> throw UserExistsException()
                    else -> {
                        exception.printStackTrace()
                        throw exception
                    }
                }
            }
        }
    }


    @Throws(AuthenticationException::class, Exception::class)
    suspend fun authenticate(user: UsuarioCred) {
        val tokenInfo: TokenInfo = clienteHttp.submitForm(
            url = "http://34.175.199.254:8000/token",
            formParameters = Parameters.build {
                append("grant_type", "password")
                append("username", user.usuario)
                append("password", user.contraseña)
            }).body()

        bearerTokenStorage.add(BearerTokens(tokenInfo.accessToken, tokenInfo.refreshToken))
    }
}



