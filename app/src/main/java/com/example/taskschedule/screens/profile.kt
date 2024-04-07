package com.example.taskschedule.screens

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.FileProvider
import androidx.core.content.FileProvider.getUriForFile
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.taskschedule.viewmodels.ActivitiesViewModel
import com.example.taskschedule.R
import com.example.taskschedule.data.Idioma
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Files

/************************************************************************
 * Composable que contiene la interfaz de los ajustes de la aplicación,
 * junto al viewmodel se encargan de toda la personalización, el viewmodel
 * se encarga de modificar el datastore.
 *************************************************************************/

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun LanguageAndThemeSelector(actividadesViewModel: ActivitiesViewModel, navController: NavController) {
    val context = LocalContext.current
    val mensajeSincronizacion by actividadesViewModel.sincronizacionMessage
    var idioma=stringResource(id = R.string.idioma)
    var selectedLanguage by remember { mutableStateOf(idioma) }
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    var showDialogPhoto by remember { mutableStateOf(false) }

    if(actividadesViewModel.errorFoto.value){
        actividadesViewModel.errorFoto.value=false
        Toast.makeText(context,"Error cargando la foto", Toast.LENGTH_LONG).show()
    }
    // Camera photo
    val imagePickerLauncherFromCamera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { pictureTaken ->
        if (pictureTaken) actividadesViewModel.cargarImagen()
        else Toast.makeText(context,"error", Toast.LENGTH_LONG).show()
    }

    // Gallery photo
    val imagePickerLauncherFromGallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { pictureTaken ->
        pictureTaken?.let { uri ->
            GlobalScope.launch(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                   actividadesViewModel.setProfileImage(BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri)))
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    val bitmap = ImageDecoder.decodeBitmap(source)

                    bitmap.let { bm -> actividadesViewModel.setProfileImage(bm) }
                }
            }
        }
    }
    fun onEditImageRequest(fromCamera: Boolean) {

        val profileImageDir = File(context.cacheDir, "images/profile/")
        Files.createDirectories(profileImageDir.toPath())

        val newProfileImagePath = File.createTempFile("usuario", ".png", profileImageDir)
        val contentUri: Uri = getUriForFile(context, "com.example.taskschedule.fileprovider", newProfileImagePath)
        actividadesViewModel.fotoPerfilPath = newProfileImagePath.path

        if (fromCamera) imagePickerLauncherFromCamera.launch(contentUri)
        else imagePickerLauncherFromGallery.launch("image/*")

    }

    val idiomas = listOf("Español", "Euskera", "English")

    LaunchedEffect(mensajeSincronizacion) {
        mensajeSincronizacion?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            actividadesViewModel.sincronizacionMessage.value = null
        }
    }

    if (showDialogPhoto) {
        AlertDialog(
            onDismissRequest = { showDialogPhoto = false },
            title = { Text("Seleccionar opción") },
            text = { Text("¿Quieres tomar una foto o seleccionar una desde la galería?") },
            confirmButton = {
                Button(onClick = {
                    onEditImageRequest(fromCamera = true)
                    showDialogPhoto = false

                }) {
                    Text("Cámara")
                }
            },
            dismissButton = {
                Button(onClick = {
                    onEditImageRequest(fromCamera = false)
                    showDialogPhoto = false

                }) {
                    Text("Galería")
                }
            }
        )
    }
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(id = R.string.selecciona), fontWeight = FontWeight.Bold)

        Box {
            TextButton(onClick = { expanded = true }) {
                Row{
                    Text(selectedLanguage, fontWeight = FontWeight.Bold)
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = stringResource(id = R.string.idioma))
                }

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
        if (!actividadesViewModel.obtenerUltUsuario().equals("")) {
            val painter = if (actividadesViewModel.fotoPerfil != null) {
                Log.d("s","se cargara la foto de perfil")
                null
            } else {
                rememberImagePainter(data = "file:///android_asset/default.jpg")
            }

            Divider()
            Spacer(modifier = Modifier.height(20.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Profile picture", fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = {   showDialogPhoto = true
                    }) {
                        // Asumiendo que tienes una imagen de perfil como Drawable o desde un URL
                        // Cambia la fuente de la imagen según tus necesidades
                        if (painter != null) {
                            // Caso para URL o recurso, donde tenemos un painter válido
                            Image(
                                painter = painter,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                            )
                        } else if (actividadesViewModel.fotoPerfil != null) {
                            Image(
                                bitmap = actividadesViewModel.fotoPerfil!!.asImageBitmap(),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                /*
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(id = R.string.claro), fontWeight = FontWeight.Bold)
                IconButton(onClick = { actividadesViewModel.cambiarOscuro(false)}, modifier = Modifier
                    .size(80.dp)
                    .aspectRatio(1f)) {
                    Icon(Icons.Filled.LightMode, contentDescription = stringResource(id = R.string.claro))
                }
            }*/
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Cerrar sesión", fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { actividadesViewModel.logout();navController.navigate("login") },
                        modifier = Modifier
                            .size(80.dp)
                            .aspectRatio(1f)
                    ) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sync cloud", fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = {
                            actividadesViewModel.sincronizar()
                        },
                        modifier = Modifier
                            .size(80.dp)
                            .aspectRatio(1f)
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = "Sync")
                    }
                }

            }
        }
    }
}

