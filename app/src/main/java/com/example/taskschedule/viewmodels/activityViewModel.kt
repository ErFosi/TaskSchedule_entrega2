package com.example.taskschedule.viewmodels;

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskschedule.MainActivity
import com.example.taskschedule.R
import com.example.taskschedule.data.Actividad
import com.example.taskschedule.data.Idioma
import com.example.taskschedule.data.ProfilePreferencesDataStore
import com.example.taskschedule.repositories.ActividadesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.taskschedule.utils.LanguageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import java.time.LocalDate
import kotlin.random.Random

/************************************************************************
 * Viewmodel que se encargará de la lógica de toda la app de forma general
 * y en especifico del apartado de lista actividades y de los ajustes
 *************************************************************************/


@HiltViewModel
class ActivitiesViewModel @Inject constructor(
private val settings:ProfilePreferencesDataStore,
private val languageManager: LanguageManager,
    private val actividadesRepo: ActividadesRepository
): ViewModel() {
    val oscuro =settings.settingsFlow.map{it.oscuro}
    val idioma = settings.settingsFlow.map {it.idioma}
    private val _actividades = actividadesRepo.getActividadesPorFecha(LocalDate.now())
    init {
        /************************************************************************
         * Cuando se lance la app, el viewmodel tratará de aplicar el ultimo lenguaje
         * guardado por el usuario en el datastore de settings
         *************************************************************************/
        this.settings.language()
        viewModelScope.launch {
            //changeLang(idioma.first())
            changeLang(Idioma.getFromCode(settings.language().first()))
            Log.d("I","Se inicia la app con el idioma:"+settings.language().first())
        }
    }
    /************************************************************************
     * Funcion que modifica la preferencia sobre el tema oscuro o claro
     *************************************************************************/
    fun cambiarOscuro(oscuro : Boolean){
        viewModelScope.launch{settings.updateOscuro(oscuro)}

    }



    /************************************************************************
     * Función que es llamada desde los composables cuando un usuario
     * selecciona otro idioma
     *************************************************************************/
    fun updateIdioma(idioma:Idioma){
        viewModelScope.launch { settings.setLanguage(idioma.code) }
        this.changeLang(idioma)
        Log.d("t","Se actualiza el idioma a"+idioma.language)
    }

    val actividades: Flow<List<Actividad>>
        get()=_actividades


    /************************************************************************
     * Función que dado un nombre crea una nueva actividad y la inserta en la
     * BD mediante el DAO
     *************************************************************************/
    fun agregarActividad(nombre: String) {

        val nuevaActividad = Actividad(
            nombre = nombre,id = 0) //el id es autogenerado ya que es autogenerate (esto se ve en data/data.kt donde está la entidad)
        viewModelScope.launch{
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
               var act=actividadesRepo.getActividadStream(id)
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
    private fun sendNotification(actividad:Actividad, context: Context){
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("id", actividad.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val stopPendingIntent= PendingIntent.getActivity(context,1,intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.Estashaciendo)+" ${actividad.nombre}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(stopPendingIntent)
            .addAction(R.drawable.stop ,context.getString(R.string.stop),stopPendingIntent)

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
}
