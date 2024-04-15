package com.example.taskschedule.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

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