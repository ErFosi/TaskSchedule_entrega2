package com.example.taskschedule.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.taskschedule.data.Idioma
import javax.inject.Inject
import javax.inject.Singleton


/*************************************************
 **            Gestor de lenguaje               **
 *************************************************/

/************************************************************************
 *Clase para la gestión del idioma de la aplicación, se usa Hilt
 * para la inyección de la dependencia relativa al singleton
 *************************************************************************/


@Singleton
class LanguageManager @Inject constructor() {


    // Method to change the App's language setting a new locale
    fun changeLang(lang: Idioma) {
        val localeList = LocaleListCompat.forLanguageTags(lang.code)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}