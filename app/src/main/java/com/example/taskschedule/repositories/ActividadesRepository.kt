package com.example.taskschedule.repositories

import com.example.taskschedule.data.Actividad
import com.example.taskschedule.data.ActividadesDao
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
 *************************************************************************/
@Singleton
class ActividadesRepository @Inject constructor(private val actividadesDao: ActividadesDao) : ActividadesRepositoryInterface{
    override suspend fun deleteActividad(actividad: Actividad) = actividadesDao.delete(actividad)


    override fun getActividadStream(id: Int): Flow<Actividad?> = actividadesDao.getActividad(id)


    override fun getActividadesStream(): Flow<List<Actividad>> = actividadesDao.getActividades()

    override suspend fun insertActividad(actividad: Actividad) = actividadesDao.insert(actividad)

    override suspend fun updateActividad(actividad: Actividad) = actividadesDao.update(actividad)

    override fun getActividadesPorFecha(fecha: LocalDate): Flow<List<Actividad>> = actividadesDao.getActividadesPorFecha(fecha)

}