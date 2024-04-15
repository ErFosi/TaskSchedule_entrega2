package com.example.taskschedule.repositories

import com.example.taskschedule.data.Ubicacion
import com.example.taskschedule.data.UbicacionDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UbicacionesRepository @Inject constructor(private val ubicacionDao: UbicacionDao) : UbicacionesRepositoryInterface {

    override fun getUbicacionesStream(): Flow<List<Ubicacion>> = ubicacionDao.getAllUbicaciones()

    override fun getUbicacionesPorActividadStream(actividadId: Int): Flow<List<Ubicacion>> = ubicacionDao.obtenerUbicacionesPorActividad(actividadId)

    override suspend fun insertUbicacion(ubicacion: Ubicacion) = ubicacionDao.insertarUbicacion(ubicacion)

    override suspend fun deleteUbicacion(ubicacion: Ubicacion) = ubicacionDao.deleteUbicacion(ubicacion)

    override suspend fun updateUbicacion(ubicacion: Ubicacion) = ubicacionDao.updateUbicacion(ubicacion)
    suspend fun deleteAllUbiss() {
        ubicacionDao.deleteAllUbis()
    }


}