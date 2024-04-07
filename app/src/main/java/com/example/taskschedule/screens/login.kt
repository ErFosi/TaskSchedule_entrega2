package com.example.taskschedule.screens

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.animation.*
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.ContentAlpha
import com.example.taskschedule.viewmodels.ActivitiesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Switch(modifier = Modifier.padding(horizontal = 4.dp),
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = colorScheme.surface,
            uncheckedThumbColor = colorScheme.surface,
            checkedTrackColor = colorScheme.primary,
            uncheckedTrackColor = colorScheme.onSurface.copy(alpha = 0.6f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(mainViewModel: ActivitiesViewModel, navController: NavHostController) {
    val backgroundColor = colorScheme.background
    val primaryColor = colorScheme.primary
    val onPrimaryColor = colorScheme.onPrimary
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nuevoUsuario by rememberSaveable { mutableStateOf(false) }
    var passwordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }

    //Errores
    var passwordError by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf(false) }
    var userExistsDialog by remember { mutableStateOf(false) }
    var serverErrorDialog by remember { mutableStateOf(false) }
    var contrasñaDebil by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        AnimatedDiagonalLinesBackground()
        if (isLoading) {
            CircularProgressIndicator() // Muestra la animación de carga
        } else {
            var showDialog = userExistsDialog || serverErrorDialog || passwordError || contrasñaDebil || authError

            val dialogTitle = if (userExistsDialog) "Error" else if(passwordError) "Contraseñas no coinciden" else if(authError) "Credenciales Invalidos" else if(contrasñaDebil) "Contraseña debil" else "Error de conexión"
            val dialogText = if (userExistsDialog) "El usuario ya existe." else if (passwordError) "Las contraseñas deben coincidir" else if(authError) "Credenciales invalidos" else if(contrasñaDebil) "La contrasña debe tener 6 caracteres, una mayuscula y minuscula"  else "Ha habido un error en la conexión con el servidor."
            //dialogos de errores
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {
                        userExistsDialog = false
                        serverErrorDialog = false
                    },
                    title = { Text(dialogTitle) },
                    text = {
                        // Asegúrate de importar androidx.compose.ui.text.style.TextAlign
                        Text(
                            dialogText,
                            modifier = Modifier.fillMaxWidth(), // Llena el ancho máximo para centrar el texto
                            textAlign = TextAlign.Center // Centra el texto
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            userExistsDialog = false
                            serverErrorDialog = false
                            passwordError = false
                            contrasñaDebil = false
                            authError=false
                        }) {
                            Text("OK")
                        }
                    }
                )
            }


            // Interfaz grafica
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Member Auth",
                        style = MaterialTheme.typography.titleLarge.copy(color = primaryColor),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Filled.Person, contentDescription = "Username")
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            errorContainerColor = if(authError) Color.Red.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                            unfocusedIndicatorColor = if(authError) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled),
                            focusedIndicatorColor = if(authError) Color.Red else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if(passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = "Password")
                        },
                        trailingIcon = {
                            val image = if (passwordVisibility)
                                Icons.Filled.Visibility
                            else
                                Icons.Filled.VisibilityOff

                            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                Icon(image, "Toggle password visibility")
                            }
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            errorContainerColor = if(passwordError or authError or contrasñaDebil) Color.Red.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                            unfocusedIndicatorColor = if(passwordError or authError or contrasñaDebil) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled),
                            focusedIndicatorColor = if(passwordError or authError or contrasñaDebil) Color.Red else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )


                    if (nuevoUsuario) {
                        TextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            singleLine = true,
                            visualTransformation = if(confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(Icons.Filled.Lock, contentDescription = "Confirm Password")
                            },
                            trailingIcon = {
                                val image = if (confirmPasswordVisibility)
                                    Icons.Filled.Visibility
                                else
                                    Icons.Filled.VisibilityOff

                                IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                                    Icon(image, "Toggle password visibility")
                                }
                            },
                            colors = TextFieldDefaults.textFieldColors(
                                errorContainerColor = if(passwordError or authError or contrasñaDebil) Color.Red.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                                unfocusedIndicatorColor = if(passwordError or authError or contrasñaDebil) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled),
                                focusedIndicatorColor = if(passwordError or authError or contrasñaDebil) Color.Red else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {


                        Button(
                            onClick = {

                                scope.launch {
                                    var respuesta="-1"
                                    var registerOK=true
                                    isLoading = true
                                    if (nuevoUsuario && confirmPassword != password) {
                                        passwordError = true
                                        registerOK=false
                                    } else if (nuevoUsuario){
                                        passwordError = false
                                        val longitudSuficiente = password.length >= 6
                                        val contieneMayuscula = password.any { it.isUpperCase() }
                                        val contieneMinuscula = password.any { it.isLowerCase() }
                                        contrasñaDebil = !(longitudSuficiente && contieneMayuscula && contieneMinuscula)
                                        if ( contrasñaDebil) {
                                            registerOK=false
                                            Log.d("D","Contraseña debil")
                                        }
                                        else{
                                            respuesta = mainViewModel.register(username, password)
                                            delay(1000)
                                            Log.d("E","Respuesta error:"+respuesta)
                                            if (respuesta == "exist") {
                                                userExistsDialog = true
                                                registerOK = false
                                            }
                                            else if (respuesta =="error"){
                                                serverErrorDialog=true
                                                registerOK=true
                                            }
                                        }

                                    }
                                    if(!nuevoUsuario or registerOK){
                                        respuesta=mainViewModel.login(username, password)
                                        Log.d("E","Respuesta error:"+respuesta)
                                        if (respuesta == "exist") {
                                            userExistsDialog = true
                                        } else if (respuesta == "error") {
                                            serverErrorDialog = true
                                        } else if (respuesta == "auth") {
                                            authError = true
                                        }
                                        else {
                                            navController.navigate("login")
                                        }

                                    }
                                    isLoading = false
                                }
                                isLoading = false
                            },
                            modifier = Modifier
                                .weight(0.6f)
                                .height(40.dp)
                                .padding(horizontal = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text(
                                text = if (nuevoUsuario) "REGISTER" else "LOGIN",
                                color = onPrimaryColor
                            )
                        }
                        Text(
                            "New User?",
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                        )


                        CustomSwitch(
                            checked = nuevoUsuario,
                            onCheckedChange = {
                                nuevoUsuario = it
                                confirmPassword = ""
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedDiagonalLinesBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    val diagonalStep = 100f
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = diagonalStep,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val lineColor = colorScheme.onSurface.copy(alpha = 0.2f)

    Canvas(modifier = Modifier.fillMaxSize()) {

        val hypotenuse = Math.hypot(size.width.toDouble(), size.height.toDouble()).toFloat()

        for (x in -hypotenuse.toInt()..(size.width.toInt() + hypotenuse.toInt()) step diagonalStep.toInt()) {
            val start = Offset(x.toFloat() - animatedOffset, -hypotenuse)
            val end = Offset(x.toFloat() + hypotenuse - animatedOffset, size.height + hypotenuse)
            drawLine(
                color = lineColor,
                start = start,
                end = end,
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}




/*
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}*/