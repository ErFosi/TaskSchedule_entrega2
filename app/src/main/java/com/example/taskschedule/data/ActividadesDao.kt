package com.example.taskschedule.data
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.taskschedule.data.Actividad
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate


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
 *************************************************************************/
@Dao
interface ActividadesDao {
    /************************************************************************
     * Agregar una actividad en la DB
     *************************************************************************/
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(actividad: Actividad)

    /************************************************************************
     * Modificar una actividad en la BD
     *************************************************************************/
    @Update
    suspend fun update(actividad:Actividad)

    /************************************************************************
     * Eliminar una actividad de la BD
     *************************************************************************/
    @Delete
    suspend fun delete(actividad:Actividad)

    /************************************************************************
     * Dado el id obtener la actividad
     *************************************************************************/
    @Query("SELECT * from actividades WHERE id=:id")
    fun getActividad(id:Int): Flow<Actividad>

    /************************************************************************
     * Obtener todas las actividades
     *************************************************************************/
    @Query("SELECT * from actividades ORDER BY tiempo DESC")
    fun getActividades(): Flow<List<Actividad>>

    /************************************************************************
     * Obtener las actividades relativas a una fecha
     *************************************************************************/
    @Query("SELECT * FROM actividades WHERE fecha = :fecha")
    fun getActividadesPorFecha(fecha: LocalDate): Flow<List<Actividad>>
}