package com.example.taskschedule.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.taskschedule.data.Actividad
import com.example.taskschedule.data.ActividadApi
import com.example.taskschedule.data.RespuestaServidor
import com.example.taskschedule.data.UsuarioCred
import com.example.taskschedule.data.UsuarioResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import javax.inject.Inject
import javax.inject.Singleton
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import com.example.taskschedule.utils.ActividadMapper.convertirApiActividadAActividad
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.util.InternalAPI
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream


@OptIn(InternalAPI::class)
@Singleton
class WebClient @Inject constructor() {
    //CIO permite corrutinas lo que permite asincronia con kotlin
    private val clienteHttp = HttpClient(CIO) {
        expectSuccess = true

        install(ContentNegotiation) { json() }


        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, _ ->
                when {
                    exception is ClientRequestException && exception.response.status == HttpStatusCode.Unauthorized -> throw AuthenticationException()
                    exception is ClientRequestException && exception.response.status == HttpStatusCode.BadRequest-> throw UserExistsException()
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
            url = "http://172.20.80.1:8000/token",
            formParameters = Parameters.build {
                append("grant_type", "password")
                append("username", user.usuario)
                append("password", user.contraseña)
            }).body()

        bearerTokenStorage.add(BearerTokens(tokenInfo.accessToken, tokenInfo.refreshToken))
    }


    @Throws(UserExistsException::class, Exception::class)
    suspend fun register(user: UsuarioCred) {
        val usuarioReg: UsuarioResponse = clienteHttp.post("http://172.20.80.1:8000/usuarios/") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            body = """
            {
                "usuario": "${user.usuario}",
                "contraseña": "${user.contraseña}"
            }
        """
        }.body<UsuarioResponse>()

    }

    @Throws(AuthenticationException::class, Exception::class)
    suspend fun obtenerActividades(): List<Actividad> {
        val actividadesApi: List<ActividadApi> = clienteHttp.get("http://172.20.80.1:8000/mis_actividades/") {
            bearerAuth(bearerTokenStorage.last().accessToken)
            accept(ContentType.Application.Json)
        }.body()

        return actividadesApi.map { convertirApiActividadAActividad(it) }

    }
    @Throws(AuthenticationException::class, Exception::class)
    suspend fun sincronizarActividades(actividadesApi: List<ActividadApi>) : Int {
        // Crear el cuerpo de la petición serializando el objeto adecuado a JSON
        val jsonActividades = Json.encodeToString(mapOf("actividades" to actividadesApi))
        Log.d("E",jsonActividades)
        try {
            val respuesta: RespuestaServidor = clienteHttp.post("http://172.20.80.1:8000/sincronizar_actividades/") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                bearerAuth(bearerTokenStorage.last().accessToken)
                body = jsonActividades
            }.body<RespuestaServidor>()

            // Verificar el mensaje de la respuesta
            if (respuesta.mensaje == "Actividades sincronizadas con éxito") {
                Log.d("Sincronización", "Exitosa: ${respuesta.mensaje}")
                return(200)
            } else {
                Log.d("Sincronización", "Respuesta inesperada del servidor: ${respuesta.mensaje}")
                return(-1)
            }

        }
        catch (e: Exception){
            Log.d("Sincronización", " no exitosa:")
            return -1
        }


    }


    @Throws(AuthenticationException::class, Exception::class)
    suspend fun descargarImagenDePerfil(): Bitmap {
        val httpResponse: HttpResponse = clienteHttp.get("http://172.20.80.1:8000/profile/image") {
            bearerAuth(bearerTokenStorage.last().accessToken)
        }

        if (httpResponse.status == HttpStatusCode.OK) {
            val bytes = httpResponse.readBytes()
            // Determina la extensión del archivo basada en el Content-Type

            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: throw Exception("No se pudo decodificar la imagen")

        } else {
            // Maneja los casos de error
            throw Exception("Error al descargar la imagen: ${httpResponse.status.description}")
        }
    }

    suspend fun uploadUserProfile(image: Bitmap) {
        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        // Obtiene el último token de acceso. Asegúrate de manejar la posibilidad de que esto pueda ser nulo o inválido.
        val token = bearerTokenStorage.last().accessToken

        // Realiza la solicitud de subida con el método POST.
        clienteHttp.submitFormWithBinaryData(
            url = "http://172.20.80.1:8000/profile/image", // Asegúrate de que la URL es correcta y usa HTTP o HTTPS según sea necesario.
            formData = formData {
                append("file", byteArray, Headers.build {
                    append(HttpHeaders.ContentType, "image/png")
                    append(HttpHeaders.ContentDisposition, "filename=profile_image.png")
                })
            }
        ) {
            method = HttpMethod.Post // Aquí especificamos que queremos usar el método POST.
            // Agrega el token de autorización en el encabezado si es necesario.
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

}



