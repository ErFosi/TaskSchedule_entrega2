package com.example.taskschedule.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/************************************************************************
 * DAO data access object, Clase para conectarse con la Base de datos,
 * esta tiene todos los metodos necesarios para obtener la información
 * necesaria por la aplicación.
 *
 *
 * Es importante remarcar que se devolveran valores tipo Flow, es decir
 * los datos vendran dados como flujos de datos ya que si este se modifica
 * podremos obtener el último resultado, además los composables se actualizarán
 * de manera automática.
 *
 * Para las ubicaciones
 *************************************************************************/


@Dao
interface UbicacionDao {
    @Insert
    suspend fun insertarUbicacion(ubicacion: Ubicacion)

    @Delete
    suspend fun deleteUbicacion(ubicacion: Ubicacion)

    @Update
    suspend fun updateUbicacion(ubicacion: Ubicacion)

    @Query("SELECT * FROM ubicaciones")
    fun getAllUbicaciones(): Flow<List<Ubicacion>>

    @Query("SELECT * FROM ubicaciones WHERE actividadId = :actividadId")
    fun obtenerUbicacionesPorActividad(actividadId: Int): Flow<List<Ubicacion>>
    @Query("DELETE FROM ubicaciones")
    suspend fun deleteAllUbis()
}
