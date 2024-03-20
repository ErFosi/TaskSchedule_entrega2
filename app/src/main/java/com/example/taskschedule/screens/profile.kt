package com.example.taskschedule.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.taskschedule.viewmodels.ActivitiesViewModel
import com.example.taskschedule.R
import com.example.taskschedule.data.Idioma

/************************************************************************
 * Composable que contiene la interfaz de los ajustes de la aplicación,
 * junto al viewmodel se encargan de toda la personalización, el viewmodel
 * se encarga de modificar el datastore.
 *************************************************************************/

@Composable
fun LanguageAndThemeSelector(actividadesViewModel: ActivitiesViewModel) {
    var idioma=stringResource(id = R.string.idioma)
    var selectedLanguage by remember { mutableStateOf(idioma) }
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }
    val idiomas = listOf("Español", "Euskera", "English")
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(id = R.string.selecciona), fontWeight = FontWeight.Bold)
        Icon(Icons.Filled.ArrowDropDown, contentDescription = stringResource(id = R.string.idioma))
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(selectedLanguage, fontWeight = FontWeight.Bold)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                idiomas.forEach { idioma ->
                    DropdownMenuItem(
                        text = { Text(idioma,fontWeight = FontWeight.Bold) },
                        onClick = {
                            //Se podria mejorar con un map,o un diccionario pero se ha considerado una implementación más sencilla
                            expanded=false
                            selectedLanguage=idioma
                            var code="es"
                            if (selectedLanguage.equals("English")){
                                code="en"
                            }
                            else if(selectedLanguage.equals("Euskera") ){
                                code="eu"
                            }

                            actividadesViewModel.updateIdioma(Idioma.getFromCode(code))

                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(id = R.string.claro), fontWeight = FontWeight.Bold)
                IconButton(onClick = { actividadesViewModel.cambiarOscuro(false)}, modifier = Modifier
                    .size(80.dp)
                    .aspectRatio(1f)) {
                    Icon(Icons.Filled.LightMode, contentDescription = stringResource(id = R.string.claro))
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(id = R.string.oscuro), fontWeight = FontWeight.Bold)
                IconButton(onClick = { actividadesViewModel.cambiarOscuro(true) }, modifier = Modifier
                    .size(80.dp)
                    .aspectRatio(1f)) {
                    Icon(Icons.Filled.DarkMode, contentDescription = stringResource(id = R.string.oscuro))
                }
            }
        }
    }
}

