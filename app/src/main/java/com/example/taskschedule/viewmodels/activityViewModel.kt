package com.example.taskschedule.viewmodels;

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskschedule.MainActivity
import com.example.taskschedule.R
import com.example.taskschedule.data.Actividad
import com.example.taskschedule.data.ActividadApi
import com.example.taskschedule.data.Idioma
import com.example.taskschedule.data.ProfilePreferencesDataStore
import com.example.taskschedule.data.UbicacionApi
import com.example.taskschedule.repositories.ActividadesRepository
import com.example.taskschedule.utils.WebClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.taskschedule.utils.LanguageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import kotlin.random.Random
import com.example.taskschedule.data.UsuarioCred
import com.example.taskschedule.repositories.UbicacionesRepository
import com.example.taskschedule.utils.AuthenticationException
import com.example.taskschedule.utils.UserExistsException


/************************************************************************
 * Viewmodel que se encargará de la lógica de toda la app de forma general
 * y en especifico del apartado de lista actividades y de los ajustes
 *************************************************************************/


@HiltViewModel
class ActivitiesViewModel @Inject constructor(
private val settings:ProfilePreferencesDataStore,
private val languageManager: LanguageManager,
    private val actividadesRepo: ActividadesRepository,
    private val ubicacionesRepo: UbicacionesRepository,
    private val autenticador: WebClient
): ViewModel() {
    val lastLogged = settings.settingsFlow.map { it.usuario }
    val oscuro = settings.settingsFlow.map { it.oscuro }
    val idioma = settings.settingsFlow.map { it.idioma }
    private val _actividades = actividadesRepo.getActividadesPorFecha(LocalDate.now())
    var errorFoto = mutableStateOf(false)
    var sincronizacionMessage = mutableStateOf<String?>(null)
        private set
    var fotoPerfil: Bitmap? by mutableStateOf(null)
    var fotoPerfilPath: String? = null
    init {
        /************************************************************************
         * Cuando se lance la app, el viewmodel tratará de aplicar el ultimo lenguaje
         * guardado por el usuario en el datastore de settings
         *************************************************************************/
        this.settings.language()
        viewModelScope.launch {
            //changeLang(idioma.first())
            changeLang(Idioma.getFromCode(settings.language().first()))
            Log.d("I", "Se inicia la app con el idioma:" + settings.language().first())
            if (!settings.user().equals("")){
                login(settings.user().first(),settings.password().first())
            }


        }


    }

    /************************************************************************
     * Funcion que modifica la preferencia sobre el tema oscuro o claro
     *************************************************************************/
    fun cambiarOscuro(oscuro: Boolean) {
        viewModelScope.launch { settings.updateOscuro(oscuro) }

    }
    fun setProfileImage(image: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            fotoPerfil = null
            try {
                autenticador.uploadUserProfile(image)
                fotoPerfil=image

            }
            catch (e :Exception){
                Log.d("E","Error de conexión")
                errorFoto.value=true
            }
        }
    }
    fun cargarImagen() {
        viewModelScope.launch(Dispatchers.IO) {
            val image = BitmapFactory.decodeFile(fotoPerfilPath!!)
            setProfileImage(image)
        }
    }
    /************************************************************************
     * Función que es llamada desde los composables cuando un usuario
     * selecciona otro idioma
     *************************************************************************/
    fun updateIdioma(idioma: Idioma) {
        viewModelScope.launch { settings.setLanguage(idioma.code) }
        this.changeLang(idioma)
        Log.d("t", "Se actualiza el idioma a" + idioma.language)
    }

    val actividades: Flow<List<Actividad>>
        get() = _actividades


    /************************************************************************
     * Función que dado un nombre crea una nueva actividad y la inserta en la
     * BD mediante el DAO
     *************************************************************************/
    fun agregarActividad(nombre: String) {

        val nuevaActividad = Actividad(
            nombre = nombre, id = 0
        ) //el id es autogenerado ya que es autogenerate (esto se ve en data/data.kt donde está la entidad)
        viewModelScope.launch {
            actividadesRepo.insertActividad(nuevaActividad)
        }
    }

    /************************************************************************
     * Función que se encarga de la logica detrás del boton de play donde
     * registra cuanto tiempo ha pasado y modifica los valores de la actividad
     * en la BD mediante el DAO
     *************************************************************************/
    fun togglePlay(actividad: Actividad, context: Context) {
        val currentActividad = actividad.copy()

        if (currentActividad.isPlaying) {
            val endTime = System.currentTimeMillis()
            val diff = (endTime - (currentActividad.startTimeMillis ?: endTime)) / 1000
            currentActividad.tiempo += diff.toInt()
            currentActividad.isPlaying = false
        } else {
            currentActividad.startTimeMillis = System.currentTimeMillis()
            currentActividad.isPlaying = true
            sendNotification(actividad, context)
        }

        viewModelScope.launch {
            actividadesRepo.updateActividad(currentActividad)
        }

    }


    /************************************************************************
     * Método para actualizar la categoría de una actividad
     *************************************************************************/

    fun updateCategoria(act: Actividad, nuevaCategoria: String) {
        viewModelScope.launch {
            actividadesRepo.updateActividad(act)
        }

    }

    /************************************************************************
     * Función encargada de eliminar la actividad, llama al DAO para modificarlo
     * en la BD.
     *************************************************************************/
    fun onRemoveClick(id: Int) {


        viewModelScope.launch {
            var act = actividadesRepo.getActividadStream(id)
            act.collect { actividad ->
                if (actividad != null) {
                    actividadesRepo.deleteActividad(actividad)
                }
                return@collect
            }
        }

    }


    /************************************************************************
     ******************************IDIOMA************************************
     *************************************************************************/


    /************************************************************************
     * Función que utiliza el LanguageUtils para la modificación del idioma
     * Locale
     * *************************************************************************/
    fun changeLang(idioma: Idioma) {
        languageManager.changeLang(idioma)
    }

    /************************************************************************
     ***************************NOTIFICACIONES*******************************
     *************************************************************************/


    /************************************************************************
     * Función donde se genera la notificación que ocurrirá siempre que se de
     * al botón de play de alguna actividad, esta guarda el id en el Extra
     * para poder gestionarlo una vez se clicka el boton de la notificación
     *************************************************************************/
    private fun sendNotification(actividad: Actividad, context: Context) {
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("id", actividad.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val stopPendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.Estashaciendo) + " ${actividad.nombre}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(stopPendingIntent)
            .addAction(R.drawable.stop, context.getString(R.string.stop), stopPendingIntent)

        with(notificationManager) {
            notify(generateRandomNotificationId(), builder.build())
        }
    }

    /************************************************************************
     * Función que genera un número aleatorio para el id de la notificación,
     * de esta manera podremos crear más de una notificación, si bien es
     * verdad que pueden coincidir la probabilidad es bajisima (1 en 10.000)
     *************************************************************************/
    fun generateRandomNotificationId(): Int {
        // Genera un ID aleatorio dentro de un rango. Se puede ajustar el rango según sea necesario.
        return Random.nextInt(1, 10000)
    }

    /************************************************************************
     * Función que usa el DAO para acceder a la BD y obtener la actividad cuya id
     * concuerde con la dada
     *************************************************************************/
    fun obtenerActividadPorId(id: Int): Flow<Actividad?> {
        // Devuelve el Flow directamente para ser coleccionado de forma asíncrona donde sea necesario
        return actividadesRepo.getActividadStream(id)
    }

    /************************************************************************
     * Información relativa a las notificaciones
     *************************************************************************/
    companion object {
        private const val CHANNEL_ID = "Task_channel"
    }

    /************************************************************************
     * Autenticarse
     *************************************************************************/

    suspend fun login(username: String, contraseña: String): String = try {
        val usuario = UsuarioCred(username, contraseña)
        autenticador.authenticate(usuario)
        Log.d("T","logged usuario: $username")
        Log.d("T","Se almacenará el usuario: $username")
        settings.saveUserCredentialsAndToken(username, contraseña)
        Log.d("C","Se ha cambiado: ${obtenerUltUsuario()}")
        delay(1000)
        var actividades:List<Actividad> =autenticador.obtenerActividades()
        for (act in actividades){
            actividadesRepo.insertActividad(act)
        }
        Log.d("S","Se procede a obtener la foto:")
        try {
            fotoPerfil=autenticador.descargarImagenDePerfil()
        }
        catch (e :Exception){
            fotoPerfil=null
            Log.d("S","Foto sin conexion")
        }
        "success"
    } catch (e: AuthenticationException) {
        Log.d("T", "Auth")
        actividadesRepo.deleteAllActividades()
        "auth"
    } catch (e: UserExistsException) {
        Log.d("T", "Exist")
        actividadesRepo.deleteAllActividades()
        "exist"
    } catch (e: Exception) {
        Log.d("T", "Otro error")
        actividadesRepo.deleteAllActividades()
        "error"
    }

    suspend fun register(username: String, contraseña: String): String = try {
        val usuario: UsuarioCred = UsuarioCred(username, contraseña)
        autenticador.register(usuario)
        "success" // Asumiendo que deseas devolver "success" si no hay excepciones.
    } catch (e: UserExistsException) {
        Log.d("T", "user already exists")
        actividadesRepo.deleteAllActividades()
        "exist"
    } catch (e: Exception) {
        Log.d("T", "Otro error")
        actividadesRepo.deleteAllActividades()
        "error"
    }

    fun logout(){
        viewModelScope.launch {
            val actividadesApi : List<ActividadApi> =getActividadesApi()
            autenticador.sincronizarActividades(actividadesApi)
            settings.saveUserCredentialsAndToken("","")
            actividadesRepo.deleteAllActividades()
            delay(200)
            fotoPerfil=null
            fotoPerfilPath=null
        }


    }

    fun sincronizar() {
        viewModelScope.launch {
            var error = autenticador.sincronizarActividades(getActividadesApi())
            if (error == 200) {
                sincronizacionMessage.value = "Sincronización exitosa"
            } else {
                val retry=login(settings.user().first(),settings.password().first())
                if (retry.equals("success")){
                    error = autenticador.sincronizarActividades(getActividadesApi())
                    if (error == 200){
                        sincronizacionMessage.value = "Sincronización exitosa"
                    }

                }
                else if (retry.equals("auth")){
                    sincronizacionMessage.value = "Error de autenticacion"
                }
                else{
                    sincronizacionMessage.value = "Error de conexión con el servidor"
                }

            }
        }
    }

    fun obtenerUltUsuario() : String{
        var user=""
        viewModelScope.launch{
            user=settings.user().first()
            Log.d("E","Ultimo user del datastore:"+user)
        }
        return(user)
    }
    suspend fun getActividadesApi(): List<ActividadApi> {
        // Asegurándonos de esperar a que la transformación se complete.
        return _actividades.first().map { actividad ->
            val ubicaciones = ubicacionesRepo.getUbicacionesPorActividadStream(actividad.id).first()
            val ubicacionesApi = ubicaciones.map { ubicacion ->
                UbicacionApi(
                    latitud = ubicacion.latitud,
                    longitud = ubicacion.longitud
                )
            }
            ActividadApi(
                id = actividad.id,
                nombre = actividad.nombre,
                tiempo = actividad.tiempo,
                categoria = actividad.categoria,
                start_time_millis = actividad.startTimeMillis,
                is_playing = actividad.isPlaying,
                id_usuario = actividad.idUsuario,
                fecha = actividad.fecha.toString(),
                ubicaciones = ubicacionesApi
            )
        }
    }
}

