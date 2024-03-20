package com.example.taskschedule.repositories

import com.example.taskschedule.data.Actividad
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/************************************************************************
 * Interfaz del repositorio que contendra el DAO
 *************************************************************************/
interface ActividadesRepositoryInterface {
    /**
     * Obtiene todas las actividades de la base de datos
     */
    fun getActividadesStream(): Flow<List<Actividad>>

    /**
     * Obtiene la actividad que coincida con el Id
     */
    fun getActividadStream(id: Int): Flow<Actividad?>

    /**
     *Inserta una actividad a la base de datos
     */
    suspend fun insertActividad(item: Actividad)

    /**
     * Elimina una actividad de la base de datos
     */
    suspend fun deleteActividad(item: Actividad)

    /**
     * Modifica una actividad
     */
    suspend fun updateActividad(item: Actividad)

    /**
     * Obtiene todas las actividades para una fecha específica de la base de datos
     */
    fun getActividadesPorFecha(fecha: LocalDate): Flow<List<Actividad>>
}