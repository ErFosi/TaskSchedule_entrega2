package com.example.taskschedule.repositories

import com.example.taskschedule.data.Ubicacion
import com.example.taskschedule.data.UbicacionDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/************************************************************************
 * Container con el DAO que usa la interfaz comentada, el container
 * se puede entender como una caja que será la que use el viewmodel para
 * acceder a la BD.
 *
 * Se podría decir que el viewmodel tiene un container que a su vez este tiene un DAO
 * que se conecta a la BD.
 *
 * Es singleton por lo que solo habrá una instancia y se podra obtener desde cualquier
 * lugar.
 * Igual que el de actividades pero para ubicaciones
 *************************************************************************/

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
