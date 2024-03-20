package com.example.taskschedule.utils

import androidx.room.TypeConverter
import java.time.LocalDate


/************************************************************************
 * Clase para la conversion de fecha de string a LocalDate ya que
 * Room no permite almacenar atributos de tipo Date pero sí strings
 * por lo que se implementa esta clase como traductor entre la aplicación
 * y Room.
 *************************************************************************/
class DateConverter {@TypeConverter
fun fromLocalDate(value: LocalDate?): String? {
    return value?.toString()
}

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }
}