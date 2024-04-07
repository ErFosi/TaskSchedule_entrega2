package com.example.taskschedule.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

import com.example.taskschedule.utils.*


val Context.profilePreferences: DataStore<Preferences> by preferencesDataStore("settings")

/************************************************************************
 * Enumeración de idiomas con sus respectivos codigos
 *************************************************************************/
enum class Idioma(val language: String, val code: String) {
    EN("English", "en"),
    EU("Euskera", "eu"),
    ES("Español", "es");
    companion object {
        /************************************************************************
         * Función para obtener el objeto Idioma a partir del codigo
         *************************************************************************/
        fun getFromCode(code: String) = when (code) {
            EU.code -> EU
            EN.code -> EN
            ES.code -> ES
            else -> EN
        }
    }
}
data class Settings(val oscuro:Boolean,
val idioma:Idioma,val usuario:String)

/************************************************************************
 * DataStore de los ajustes, este es un singleton y almacenará los valores
 * del tema y el idioma.
 *
 *************************************************************************/
@Singleton
class ProfilePreferencesDataStore @Inject constructor(
    @ApplicationContext context: Context
){
private val profilePreferences=context.profilePreferences
    @Inject
    lateinit var aesCipher: AESCipher
    private object PreferencesKeys {
        val OSCURO_KEY = booleanPreferencesKey("oscuro")
        val IDIOMA_KEY = stringPreferencesKey("idioma")
        val USUARIO_KEY = stringPreferencesKey("usuario")
        val CONTRASENA_KEY = stringPreferencesKey("contrasena")
    }

    val settingsFlow= profilePreferences.data
        .catch {  exception ->
            if (exception is IOException){
                emit(emptyPreferences())
            }else {
                throw exception
            }
        }.map { preferences->
            val oscuro = preferences[PreferencesKeys.OSCURO_KEY] ?: true
            val idioma:Idioma = Idioma.getFromCode(Locale.getDefault().language.lowercase())?:Idioma.EN
            val usuario=preferences[PreferencesKeys.USUARIO_KEY] ?: "error"
            Settings(oscuro,idioma,usuario)
        }

    /************************************************************************
     * Función para modificar el tema
     *************************************************************************/
    suspend fun updateOscuro(oscuro: Boolean){
        profilePreferences.edit { settings ->
            settings[PreferencesKeys.OSCURO_KEY]= oscuro

        }
    }
    suspend fun updateUsuario(usuario: String){
        profilePreferences.edit { settings ->
            settings[PreferencesKeys.USUARIO_KEY]= ""

        }
    }

    /************************************************************************
     * Función para guardar datos login
     *************************************************************************/
    suspend fun saveUserCredentialsAndToken(usuario: String, contrasena: String) {
        Log.d("E","Intento de almacenar cifrado")

        val encryptedUser = aesCipher.encryptData("userKeyAlias", usuario)
        val encryptedPassword = aesCipher.encryptData("passwordKeyAlias", contrasena)
        Log.d("E","Usuario encriptado:"+encryptedUser)

        Log.d("T", "Modificado en datastore")
        profilePreferences.edit { preferences ->
            preferences[PreferencesKeys.USUARIO_KEY] = encryptedUser
            preferences[PreferencesKeys.CONTRASENA_KEY] = encryptedPassword
        }
    }



    /************************************************************************
     *La siguiente función devuelve un flow del idioma preferido por el usuario
     *************************************************************************/
    fun language(): Flow<String> = profilePreferences.data.map { preferences -> preferences[PreferencesKeys.IDIOMA_KEY]?: Locale.getDefault().language }

    /************************************************************************
     * Función para modificar el lenguaje
     *************************************************************************/
    suspend fun setLanguage(code: String) {
        profilePreferences.edit { settings ->  settings[PreferencesKeys.IDIOMA_KEY]=code}
        Log.d("P","Se esta cambiando el lenguaje en el dataStore"+code)
    }

    fun user(): Flow<String> = profilePreferences.data.map { preferences ->
        val encryptedUser = preferences[PreferencesKeys.USUARIO_KEY] ?: ""
        if (encryptedUser.isNotEmpty()) {
            try {
                aesCipher.decryptData("userKeyAlias", encryptedUser)
            } catch (e: Exception) {
                Log.d("E","Error en el descifrado")
                ""
            }
        } else {
            ""
        }
    }

    fun password(): Flow<String> = profilePreferences.data.map { preferences ->
        val encryptedPassword = preferences[PreferencesKeys.CONTRASENA_KEY] ?: ""
        if (encryptedPassword.isNotEmpty()) {
            try {
                aesCipher.decryptData("passwordKeyAlias", encryptedPassword)
            } catch (e: Exception) {
                Log.d("E","Error en el descifrado")
                ""

            }
        } else {
            ""
        }
    }

}