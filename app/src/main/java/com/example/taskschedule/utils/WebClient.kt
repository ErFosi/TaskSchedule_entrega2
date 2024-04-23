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
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.util.InternalAPI
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream


/************************************************************************
 * Clase singleton que se encarga de mandar todas las peticiones a la API
 *************************************************************************/

@OptIn(InternalAPI::class)
@Singleton
class WebClient @Inject constructor() {
/************************************************************************
 * Inicialización del cliente http
 *************************************************************************/
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

/************************************************************************
 * Función que manda usuario y contraseña al servidor, emite dos errores
 * AuthenticationException y Exception, cuando se da el primero los credenciales
 * son incorrectos, si no se asume que no se ha podido entablecer conexión
 * con el servidor.
 *
 * Además, si se consigue hacer login se almacena el token Oauth (Última linea)
 *************************************************************************/
    @Throws(AuthenticationException::class, Exception::class)
    suspend fun authenticate(user: UsuarioCred) {
        val tokenInfo: TokenInfo = clienteHttp.submitForm(
            url = "http://34.175.97.114:8000/token",
            formParameters = Parameters.build {
                append("grant_type", "password")
                append("username", user.usuario)
                append("password", user.contraseña)
            }).body()

        bearerTokenStorage.add(BearerTokens(tokenInfo.accessToken, tokenInfo.refreshToken))
    }
/************************************************************************
 * Función que manda la peticion de registrar un usuario, emite dos errores
 * UserExistsException y Exception, cuando se da el primero significa que
 * ya existe el usuario en la BD,si no 
 *se asume que no se ha podido entablecer conexión con el servidor.
 *
 * Además, si se consigue hacer login se almacena el token Oauth (Última linea)
 *************************************************************************/

    @Throws(UserExistsException::class, Exception::class)
    suspend fun register(user: UsuarioCred) {
        val usuarioReg: UsuarioResponse = clienteHttp.post("http://34.175.97.114:8000/usuarios/") {
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

    /************************************************************************
     * Lanza la petición para obtener las actividades del usuario loggeado
     * emite dos exceptions, Exception si hay errores de conexión ó AuthenticationException
     * si el token no es valido y hace falta pedir otro
     *************************************************************************/

    @Throws(AuthenticationException::class, Exception::class)
    suspend fun obtenerActividades(): List<ActividadApi> {
        val actividadesApi: List<ActividadApi> = clienteHttp.get("http://34.175.97.114:8000/mis_actividades/") {
            bearerAuth(bearerTokenStorage.last().accessToken)
            accept(ContentType.Application.Json)
        }.body()

        return actividadesApi

    }


    /************************************************************************
     * Lanza la petición para subir los datos al servidor,
     * emite dos exceptions, Exception si hay errores de conexión ó AuthenticationException
     * si el token no es valido y hace falta pedir otro
     *************************************************************************/
    @Throws(AuthenticationException::class, Exception::class)
    suspend fun sincronizarActividades(actividadesApi: List<ActividadApi>) : Int {
        val jsonActividades = Json.encodeToString(mapOf("actividades" to actividadesApi))
        Log.d("E",jsonActividades)
        try {
            val respuesta: RespuestaServidor = clienteHttp.post("http://34.175.97.114:8000/sincronizar_actividades/") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                bearerAuth(bearerTokenStorage.last().accessToken)
                body = jsonActividades
            }.body<RespuestaServidor>()
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
    /************************************************************************
     * Lanza la petición para descargar la imagen de perfil del usuario autenticado,
     * emite dos exceptions, Exception si hay errores de conexión ó AuthenticationException
     * si el token no es valido y hace falta pedir otro
     *************************************************************************/

    @Throws(AuthenticationException::class, Exception::class)
    suspend fun descargarImagenDePerfil(): Bitmap {
        val httpResponse: HttpResponse = clienteHttp.get("http://34.175.97.114:8000/profile/image") {
            bearerAuth(bearerTokenStorage.last().accessToken)
        }

        if (httpResponse.status == HttpStatusCode.OK) {
            val bytes = httpResponse.readBytes()

            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: throw Exception("No se pudo decodificar la imagen")

        } else {
            throw Exception("Error al descargar la imagen: ${httpResponse.status.description}")
        }
    }
    /************************************************************************
     * Lanza la petición para subir una imagen al servidor
     *************************************************************************/
    suspend fun uploadUserProfile(image: Bitmap) {
        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 75, stream)
        val byteArray = stream.toByteArray()
        val token = bearerTokenStorage.last().accessToken

        clienteHttp.submitFormWithBinaryData(
            url = "http://34.175.97.114:8000/profile/image", 
            formData = formData {
                append("file", byteArray, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=profile_image.jpg")
                })
            }
        ) {
            method = HttpMethod.Post // Aquí especificamos que queremos usar el método POST.
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }
    /************************************************************************
     * Lanza la petición para suscribirse al FCM enviando el token.
     *************************************************************************/
    suspend fun subscribeUser(FCMClientToken: String) {
        val token = bearerTokenStorage.last().accessToken
        clienteHttp.post("http://34.175.97.114:8000/sub_a_act") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(mapOf("fcm_client_token" to FCMClientToken))
        }
    }
    /************************************************************************
     * Lanza la petición para testear el FCM
     *************************************************************************/
    suspend fun testFCM() {
        val client = HttpClient()
        try {
            val token = bearerTokenStorage.last().accessToken
            val response: HttpResponse = clienteHttp.post("http://34.175.97.114:8000/testactividades") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
            }
            println("Response status: ${response.status}")
        } catch (e: Exception) {
            println("Error sending the request: ${e.message}")
        } finally {
            client.close()
        }
    }

}



