package com.example.taskschedule.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.FileProvider
import androidx.core.content.FileProvider.getUriForFile
import androidx.navigation.NavController
import androidx.wear.compose.material.CircularProgressIndicator
import coil.compose.rememberImagePainter
import com.example.taskschedule.viewmodels.ActivitiesViewModel
import com.example.taskschedule.R
import com.example.taskschedule.data.Idioma
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
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
    var idioma = stringResource(id = R.string.idioma)
    var selectedLanguage by remember { mutableStateOf(idioma) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    var showDialogPhoto by remember { mutableStateOf(false) }

    if (actividadesViewModel.errorFoto.value) {
        actividadesViewModel.errorFoto.value = false
        Toast.makeText(context, stringResource(id = R.string.errorFoto), Toast.LENGTH_LONG).show()
    }

    
/************************************************************************
 * Orientación de la foto
 *************************************************************************/

    fun rotateImageIfNeeded(bitmap: Bitmap, uri: Uri, context: Context): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri)
        val exifInterface = inputStream?.let {
            ExifInterface(it)
        }
        val orientation = exifInterface?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val rotationAngle = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
        return if (rotationAngle != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotationAngle.toFloat())
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    fun rotateBitmap(source: Bitmap): Bitmap {
        Log.d("Rotar", "Se rota la foto")
        val matrix = Matrix()
        matrix.postRotate(90f)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun rotateBitmapIfNeeded(sourcePath: String): Bitmap {
        val bitmap = BitmapFactory.decodeFile(sourcePath)
        val exif = ExifInterface(sourcePath)
        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    // Camera photo
    val imagePickerLauncherFromCamera =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { pictureTaken ->
            if (pictureTaken) {
                
                val bitmap = BitmapFactory.decodeFile(actividadesViewModel.fotoPerfilPath)
                val rotatedBitmap =
                    actividadesViewModel.fotoPerfilPath?.let { rotateBitmapIfNeeded(it) }

                val outputStream = FileOutputStream(actividadesViewModel.fotoPerfilPath)
                rotatedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                outputStream.flush()
                outputStream.close()

                actividadesViewModel.setProfileImage(rotatedBitmap!!)
            } else {
                Toast.makeText(context, "Error capturing the photo", Toast.LENGTH_LONG).show()
            }
        }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    //Comprime la foto a menos de 400KB
    fun compressImageToUnder400KB(bitmap: Bitmap): ByteArray {
        var quality = 100
        var compressedImage = ByteArrayOutputStream()
        do {
            compressedImage.reset() // Limpiar el ByteArrayOutputStream para la nueva prueba de compresión
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, compressedImage)
            if (quality > 10) quality -= 10 // Reducir la calidad en incrementos del 10%
        } while (compressedImage.size() > 400 * 1024 && quality > 10) // Continuar mientras el archivo sea mayor a 400KB y la calidad por encima de 10%

        return compressedImage.toByteArray()
    }

    // Obtener foto de la galería
    val imagePickerLauncherFromGallery =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { pictureTaken ->
            pictureTaken?.let { uri ->
                GlobalScope.launch(Dispatchers.IO) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {

                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = false
                            inSampleSize = calculateInSampleSize(this, 100, 100)
                        }
                        var bitmap = BitmapFactory.decodeStream(
                            context.contentResolver.openInputStream(uri),
                            null,
                            options
                        )
                        bitmap = rotateImageIfNeeded(bitmap!!, uri, context)
                        val finalImage = compressImageToUnder400KB(bitmap)
                        actividadesViewModel.setProfileImage(
                            BitmapFactory.decodeByteArray(
                                finalImage,
                                0,
                                finalImage.size
                            )
                        )
                    } else {

                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        var bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                            decoder.setTargetSize(100, 100)
                            // Habilitar la corrección automática de la orientación
                            if (info.colorSpace != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                decoder.setTargetColorSpace(info.colorSpace)
                            }
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        }


                        val finalImage = compressImageToUnder400KB(bitmap)
                        actividadesViewModel.setProfileImage(
                            BitmapFactory.decodeByteArray(
                                finalImage,
                                0,
                                finalImage.size
                            )
                        )
                    }
                }
            }
        }

    //Editar la foto de perfil

    fun onEditImageRequest(fromCamera: Boolean) {

        val profileImageDir = File(context.cacheDir, "images/profile/")
        Files.createDirectories(profileImageDir.toPath())

        val newProfileImagePath = File.createTempFile("usuario", ".jpeg", profileImageDir)
        val contentUri: Uri =
            getUriForFile(context, "com.example.taskschedule.fileprovider", newProfileImagePath)
        actividadesViewModel.fotoPerfilPath = newProfileImagePath.path

        if (fromCamera) imagePickerLauncherFromCamera.launch(contentUri)
        else imagePickerLauncherFromGallery.launch("image/*")

    }
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .size(50.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(40.dp)) // Ajusta este valor según tus necesidades
        }
    } else {
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
                title = {
                    Text(
                        stringResource(id = R.string.selecOpc),
                        modifier = Modifier.fillMaxWidth(), // Extiende el Text a lo ancho del diálogo
                        textAlign = TextAlign.Center // Centra el texto dentro de su contenedor
                    )
                },
                text = { Text(stringResource(R.string.selecOpc)) },
                confirmButton = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    ) {
                        Button(onClick = {
                            onEditImageRequest(fromCamera = true)
                            showDialogPhoto = false
                        },modifier=Modifier.fillMaxWidth(),shape= RoundedCornerShape(3.dp)) {
                            Text(stringResource(R.string.cam))
                        }
                    }
                },
                dismissButton = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 4.dp).fillMaxWidth().width(20.dp)
                    ) {
                        Button(onClick = {
                            onEditImageRequest(fromCamera = false)
                            showDialogPhoto = false
                        },modifier=Modifier.fillMaxWidth(),
                            shape= RoundedCornerShape(3.dp)
                        ) {
                            Text(stringResource(R.string.gal))
                        }
                    }
                }
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Text(stringResource(id = R.string.selecciona), fontWeight = FontWeight.Bold)

            Box {
                TextButton(onClick = { expanded = true }) {
                    Row {
                        Text(selectedLanguage, fontWeight = FontWeight.Bold)
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = stringResource(id = R.string.idioma)
                        )
                    }

                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    idiomas.forEach { idioma ->
                        DropdownMenuItem(
                            text = { Text(idioma, fontWeight = FontWeight.Bold) },
                            onClick = {
                                //Se podria mejorar con un map,o un diccionario pero se ha considerado una implementación más sencilla
                                expanded = false
                                selectedLanguage = idioma
                                var code = "es"
                                if (selectedLanguage.equals("English")) {
                                    code = "en"
                                } else if (selectedLanguage.equals("Euskera")) {
                                    code = "eu"
                                }

                                actividadesViewModel.updateIdioma(Idioma.getFromCode(code))

                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(id = R.string.claro), fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { actividadesViewModel.cambiarOscuro(false) }, modifier = Modifier
                            .size(80.dp)
                            .aspectRatio(1f)
                    ) {
                        Icon(
                            Icons.Filled.LightMode,
                            contentDescription = stringResource(id = R.string.claro)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(id = R.string.oscuro), fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { actividadesViewModel.cambiarOscuro(true) }, modifier = Modifier
                            .size(80.dp)
                            .aspectRatio(1f)
                    ) {
                        Icon(
                            Icons.Filled.DarkMode,
                            contentDescription = stringResource(id = R.string.oscuro)
                        )
                    }
                }
            }

            //Comprueba si hay usuario con sesión iniciada, si la hay muestra los ajustes de usuario
            if (!actividadesViewModel.obtenerUltUsuario().equals("")) {
                val painter = if (actividadesViewModel.fotoPerfil != null) {
                    Log.d("s", "se cargara la foto de perfil")
                    null
                } else {
                    rememberImagePainter(data = "file:///android_asset/default.jpg")
                }

                Divider()
                Spacer(modifier = Modifier.height(20.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.fotoP), fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = {
                            showDialogPhoto = true
                        }) {
                            if (painter != null) {
                                Image(
                                    painter = painter,
                                    contentDescription = stringResource(R.string.fotoP),
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                )
                            } else if (actividadesViewModel.fotoPerfil != null) {
                                Image(
                                    bitmap = actividadesViewModel.fotoPerfil!!.asImageBitmap(),
                                    contentDescription = stringResource(R.string.fotoP),
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
                        Text(stringResource(R.string.cerSes), fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = {
                                scope.launch{
                                    isLoading=true


                                    actividadesViewModel.logout();
                                    delay(2500)
                                    isLoading=false
                                    navController.navigate("login")

                                }
                                isLoading=false



                                      },
                            modifier = Modifier
                                .size(80.dp)
                                .aspectRatio(1f)
                        ) {
                            Icon(Icons.Filled.ExitToApp, contentDescription = stringResource(R.string.cerSes))
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.sync), fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = {
                                actividadesViewModel.sincronizar()
                            },
                            modifier = Modifier
                                .size(80.dp)
                                .aspectRatio(1f)
                        ) {
                            Icon(Icons.Filled.CloudUpload, contentDescription = stringResource(R.string.sync))
                        }
                    }

                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Test FCM", fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = {
                                actividadesViewModel.probarFCM()
                            },
                            modifier = Modifier
                                .size(80.dp)
                                .aspectRatio(1f)
                        ) {
                            Icon(Icons.Filled.LocalFireDepartment, contentDescription = "FCM")
                        }
                    }


                }
            }
        }
    }
}

