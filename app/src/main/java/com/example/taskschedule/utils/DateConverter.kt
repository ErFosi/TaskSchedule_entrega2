package com.example.taskschedule.utils

import androidx.room.TypeConverter
import com.example.taskschedule.data.Actividad
import com.example.taskschedule.data.ActividadApi
import java.time.LocalDate


/************************************************************************
 * Clase para la conversion de fecha de string a LocalDate ya que
 * Room no permite almacenar atributos de tipo Date pero sí strings
 * por lo que se implementa esta clase como traductor entre la aplicación
 * y Room.
 *************************************************************************/
class DateConverter {@TypeConverter
fun fromLocalDate(value: LocalDate?): String? {
    return value?.toString()
}

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }


}



/************************************************************************
 * Clase singleton para el mapeo de actividades ya que la fecha esta
 * en diferente formato, actua como traductor entre ambos tipos de actividad.
 *************************************************************************/
object ActividadMapper {

    fun convertirApiActividadAActividad(actividadApi: ActividadApi): Actividad {
        val fecha = LocalDate.parse(actividadApi.fecha) // Asumiendo formato ISO_LOCAL_DATE

        return Actividad(
            id = actividadApi.id,
            nombre = actividadApi.nombre,
            tiempo = actividadApi.tiempo,
            categoria = actividadApi.categoria,
            startTimeMillis = actividadApi.start_time_millis,
            isPlaying = actividadApi.is_playing,
            idUsuario = actividadApi.id_usuario,
            fecha = fecha
        )
    }
    fun convertirActividadAApiActividad(actividad: Actividad): ActividadApi {
        val fechaString = actividad.fecha.toString() 


        return ActividadApi(
            id = actividad.id,
            nombre = actividad.nombre,
            tiempo = actividad.tiempo,
            categoria = actividad.categoria,
            start_time_millis = actividad.startTimeMillis,
            is_playing = actividad.isPlaying,
            id_usuario = actividad.idUsuario,
            fecha = fechaString
        )
    }
}
