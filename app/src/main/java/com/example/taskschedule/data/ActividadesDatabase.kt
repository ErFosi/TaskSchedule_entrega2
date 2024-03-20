package com.example.taskschedule.data
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.taskschedule.utils.DateConverter

/************************************************************************
 * Aqui definimos la base de datos de ROOM y sus entidades, en este caso
 * Actividad es la unica entidad que almacenaremos
 *************************************************************************/
@Database(entities = [Actividad::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class ActividadesDatabase: RoomDatabase() {
    abstract fun actividadDao(): ActividadesDao

}