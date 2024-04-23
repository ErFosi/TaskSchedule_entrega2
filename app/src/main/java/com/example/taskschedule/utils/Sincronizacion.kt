package com.example.taskschedule.utils

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.contentColorFor
import com.example.taskschedule.R
import com.example.taskschedule.data.Actividad
import com.example.taskschedule.data.ActividadApi
import com.example.taskschedule.data.UbicacionApi
import com.example.taskschedule.repositories.ActividadesRepository
import com.example.taskschedule.repositories.UbicacionesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


import android.app.Service
import android.content.pm.PackageManager
import android.os.Build

import kotlinx.coroutines.*

import com.example.taskschedule.widget.Widget

/************************************************************************
 * Receptor para el servicio en segundo plano de la sincronización
 *************************************************************************/

class SincronizacionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, SincronizacionService::class.java)
        context.startForegroundService(serviceIntent)

    }
    }

/************************************************************************
 * Clase encargada de gestionar el servicio en segundo en plano de sincronización
 *************************************************************************/
@AndroidEntryPoint
class SincronizacionService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    @Inject
    lateinit var actividadesRepo: ActividadesRepository
    @Inject
    lateinit var ubicacionesRepo: UbicacionesRepository
    @Inject
    lateinit var httpClient: WebClient

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        Log.d("e", "Test service")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
        serviceScope.launch {
            supervisorScope {
                try {
                    sincronizar()
                    val successNotification = createSuccessNotification()
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(1, successNotification)

                } catch (e: Exception) {
                    Log.e("SincronizacionService", "Error en sincronización al lanzar el intent", e)
                    val errorNotification = createErrorNotification()
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(1, errorNotification)

                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()  
        Log.d("SincronizacionService", "Service destroyed")
    }
    /************************************************************************
     * Crea un canal de notificaciones
     *************************************************************************/
    private fun createNotification(): Notification {
        val notificationChannelId = "Task_channel"
        // Configuración del canal de notificación y creación de la notificación
        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Sincronización en Progreso")
            .setContentText("Tus datos se están sincronizando.")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    /************************************************************************
     *Función que se encarga de la sincronización, similar al del viewmodel
     *************************************************************************/
    private suspend fun sincronizar() {
        try {
            val actividades = actividadesRepo.getActividadesStream().first()
            withContext(NonCancellable) {
                val actividadesApi = getActividadesApi(actividades)
                val error = httpClient.sincronizarActividades(actividadesApi)
                if (error == 200) {
                    Log.d("SincronizacionService", "Sincronización exitosa")
                } else {
                    Log.e("SincronizacionService", "Error en sincronización: $error")
                }
            }
        } catch (e: CancellationException) {
            Log.e("SincronizacionService", "Sincronización cancelada", e)
        } catch (e: Exception) {
            Log.e("SincronizacionService", "Error en sincronización", e)
        }
    }
    /************************************************************************
     * Notificación exitosa
     *************************************************************************/
    private fun createSuccessNotification(): Notification {
        val notificationChannelId = "Task_channel"
        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Sincronización Completada")
            .setContentText("La sincronización ha terminado exitosamente.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Cambia esto por un ícono adecuado
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }
    /************************************************************************
     * Notificación error
     *************************************************************************/
    private fun createErrorNotification(): Notification {
        val notificationChannelId = "Task_channel"
        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Error de Sincronización")
            .setContentText("Hubo un error durante la sincronización.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Cambia esto por un ícono adecuado
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }
    /************************************************************************
     * FUnción para obtener la lista de ActividadApi, igual que en el viewmodel
     *************************************************************************/
    suspend fun getActividadesApi(acts: List<Actividad>): List<ActividadApi> {
        return acts.map { actividad ->
            val ubicaciones = ubicacionesRepo.getUbicacionesPorActividadStream(actividad.id).first()
            val ubicacionesApi = ubicaciones.map { ubicacion ->
                UbicacionApi(latitud = ubicacion.latitud, longitud = ubicacion.longitud)
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
