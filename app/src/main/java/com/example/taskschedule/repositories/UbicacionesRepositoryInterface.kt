package com.example.taskschedule.repositories

import com.example.taskschedule.data.Ubicacion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/************************************************************************
 * Interfaz del repositorio que contendra el DAO de ubicaciones
 *************************************************************************/


interface UbicacionesRepositoryInterface {
    /**
     * Obtiene todas las ubicaciones de la base de datos
     */
    fun getUbicacionesStream(): Flow<List<Ubicacion>>

    /**
     * Obtiene las ubicaciones que coinciden con el Id de la actividad
     */
    fun getUbicacionesPorActividadStream(actividadId: Int): Flow<List<Ubicacion>>

    /**
     * Inserta una ubicación a la base de datos
     */
    suspend fun insertUbicacion(ubicacion: Ubicacion)

    /**
     * Elimina una ubicación de la base de datos
     */
    suspend fun deleteUbicacion(ubicacion: Ubicacion)

    /**
     * Modifica una ubicación
     */
    suspend fun updateUbicacion(ubicacion: Ubicacion)

}
