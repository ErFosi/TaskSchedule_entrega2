package com.example.taskschedule.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.runtime.Immutable
import java.time.LocalDate
import kotlinx.serialization.Serializable

/************************************************************************
 * Entidad de la base de datos, el id es autogenerado por lo que da igual
 * cual se le pase.
 *
 * Es inmutable por lo que para las modificaciones se reemplazan por copias
 * que sí son mutables
 *************************************************************************/
@Immutable
@Entity(tableName = "actividades")
data class Actividad(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val nombre: String,
    var tiempo: Int = 0,
    var categoria: String = "Otros",
    var startTimeMillis: Long = 0,
    var isPlaying: Boolean = false,
    val idUsuario: Int=0, //Modificar en la siguiente entrega con el login
    val fecha: LocalDate = LocalDate.now()
)

@Entity(tableName = "ubicaciones")
data class Ubicacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val actividadId: Int,
    val latitud: Double,
    val longitud: Double
)
@Serializable
data class UsuarioCred(
    val usuario:String,
    val contraseña:String
)
@Serializable
data class UsuarioResponse(
    val usuario: String,
    val id: Int,
)

@Serializable
data class ActividadApi(
    val id: Int,
    val nombre: String,
    val tiempo: Int,
    val categoria: String,
    val start_time_millis: Long,
    val is_playing: Boolean,
    val id_usuario: Int,
    val fecha: String, // Suponiendo que la fecha viene en formato de String
    val ubicaciones: List<UbicacionApi> = emptyList() // Agregar este campo
)

@Serializable
data class UbicacionApi(
    val latitud: Double,
    val longitud: Double
)


@Serializable
data class RespuestaServidor(
    val mensaje: String
)