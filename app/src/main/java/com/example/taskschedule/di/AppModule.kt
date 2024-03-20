package com.example.taskschedule.di
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import com.example.taskschedule.data.ActividadesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/************************************************************************
 * Clase que se encarga de las dependencias con Hilt. En esta app
 * solo se usa singleton y en la función provides se puede ver lo que Hilt
 * inyecta automaticamente en las llamadas a esos singleton por ejemplo.
 *
 * Hilt se encarga de inyectar automáticamente estas dependencias donde se necesiten,
 * según se define en este módulo.
 *************************************************************************/

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun providesDatabase(@ApplicationContext app: Context) =
        Room.databaseBuilder(app, ActividadesDatabase::class.java, "actividades_db")
            .createFromAsset("database/actividades_db.db") //Día con datos cargados 14 de Marzo
            .fallbackToDestructiveMigration()
            .build()

    @Singleton
    @Provides
    fun provideActividades(db:ActividadesDatabase)=db.actividadDao()
}