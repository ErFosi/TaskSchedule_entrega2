package com.example.taskschedule

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.taskschedule.screens.DatePickerComposable
import com.example.taskschedule.screens.ListaActividadesUI
import com.example.taskschedule.screens.LanguageAndThemeSelector
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.compose.TaskSchedule
import com.example.taskschedule.viewmodels.ActivitiesViewModel
import com.example.taskschedule.viewmodels.CalendarViewModel

//import com.example.taskschedule.ui.theme.TaskScheduleTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskschedule.screens.LoginScreen
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.taskschedule.utils.SincronizacionReceiver
import com.example.taskschedule.widget.MyAppWidgetReceiver
import com.example.taskschedule.widget.Widget
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import java.util.Calendar
import java.util.concurrent.TimeUnit


enum class NotificationChannelID(val id: String) {
    GENERAL_CHANNEL("general_channel")
}
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<ActivitiesViewModel>()
    private val REQUEST_CODE_POST_NOTIFICATIONS = 1001
    private val REQUEST_CODE_CALENDAR_PERMISSIONS = 101
    private val REQUEST_CODE_LOCATION_PERMISSIONS = 2
    private val REQUEST_CODE_FOREGROUND_SERVICE = 1002
    private val REQUEST_CODE_GET_ACCOUNTS = 1002
    private val REQUEST_CODE_CAMERA_PERMISSION = 1004
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val allPermissions = mapOf(
        Manifest.permission.POST_NOTIFICATIONS to REQUEST_CODE_POST_NOTIFICATIONS,
        Manifest.permission.READ_CALENDAR to REQUEST_CODE_CALENDAR_PERMISSIONS,
        Manifest.permission.WRITE_CALENDAR to REQUEST_CODE_CALENDAR_PERMISSIONS,
        Manifest.permission.ACCESS_FINE_LOCATION to REQUEST_CODE_LOCATION_PERMISSIONS,
        Manifest.permission.ACCESS_COARSE_LOCATION to REQUEST_CODE_LOCATION_PERMISSIONS,
        Manifest.permission.GET_ACCOUNTS to REQUEST_CODE_GET_ACCOUNTS,
        Manifest.permission.CAMERA to REQUEST_CODE_CAMERA_PERMISSION
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /************************************************************************
         * Creación del canal de notificaciones
         *************************************************************************/

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkAndRequestAllPermissions()
        val name = getString(R.string.channel_act)
        val descriptionText = getString(R.string.channel_desc)

        val mChannel = NotificationChannel("Task_channel", name, NotificationManager.IMPORTANCE_LOW)
        mChannel.description = descriptionText
        onNewIntent(intent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
        createNotificationChannel()


        configurarAlarma(this.baseContext)
        //updateWidget()




        /************************************************************************
         * Composable de la actividad
         *************************************************************************/
        setContent {
            TaskSchedule(useDarkTheme  = viewModel.oscuro.collectAsState(initial = true).value) {
                TaskScheduleApp(viewModel = viewModel)
            }

        }
    }
    override fun onPause() {
        super.onPause()
        Log.d("E","Update widgets")
        updateWidget()
        Log.d("Widget init","Updated widgets")
    }
    override fun onStop() {
        super.onStop()
        Log.d("E","Update widgets")
        updateWidget()
        Log.d("E","Updated widgets")
    }

    private fun updateWidget() {
        try {
            val intent = Intent(this, MyAppWidgetReceiver::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(ComponentName(application, Widget::class.java))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e("WidgetUpdateError", "Error updating widget: ${e.message}")
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name =  getString(R.string.channel_act) // El nombre amigable para el usuario del canal
            val descriptionText =  getString(R.string.channel_desc)  // Descripción del canal
            val importance = NotificationManager.IMPORTANCE_LOW // Importancia del canal

            val channel = NotificationChannel(NotificationChannelID.GENERAL_CHANNEL.id, name, importance).apply {
                description = descriptionText
            }

            // Registra el canal con el sistema
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkAndRequestAllPermissions() {
        val allPermissions = mapOf(
            Manifest.permission.POST_NOTIFICATIONS to REQUEST_CODE_POST_NOTIFICATIONS,
            Manifest.permission.READ_CALENDAR to REQUEST_CODE_CALENDAR_PERMISSIONS,
            Manifest.permission.WRITE_CALENDAR to REQUEST_CODE_CALENDAR_PERMISSIONS,
            Manifest.permission.ACCESS_FINE_LOCATION to REQUEST_CODE_LOCATION_PERMISSIONS,
            Manifest.permission.ACCESS_COARSE_LOCATION to REQUEST_CODE_LOCATION_PERMISSIONS,
            //Manifest.permission.GET_ACCOUNTS to REQUEST_CODE_GET_ACCOUNTS
            Manifest.permission.CAMERA to REQUEST_CODE_CAMERA_PERMISSION

        )


        val neededPermissions = allPermissions.filter {
            ContextCompat.checkSelfPermission(this, it.key) != PackageManager.PERMISSION_GRANTED
        }

        if (neededPermissions.isNotEmpty()) {
            val rationaleNeeded = neededPermissions.any { ActivityCompat.shouldShowRequestPermissionRationale(this, it.key) }
            if (rationaleNeeded) {
                showGeneralRationaleDialog(neededPermissions.keys.toList())
            } else {
                requestAllNeededPermissions(neededPermissions)
            }
        }
    }

    private fun requestAllNeededPermissions(permissions: Map<String, Int>) {
        val permissionsArray = permissions.keys.toTypedArray()
        val requestCode = permissions.values.first()  // You could enhance this to handle multiple request codes if needed
        ActivityCompat.requestPermissions(this, permissionsArray, requestCode)
    }

    private fun showGeneralRationaleDialog(permissions: List<String>) {
        AlertDialog.Builder(this)
            .setTitle("Multiple Permissions Needed")
            .setMessage("This app needs several permissions to function properly. Please grant them.")
            .setPositiveButton("OK") { dialog, id ->
                requestAllNeededPermissions(permissions.map { it to allPermissions[it]!! }.toMap())
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }



        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Todos los permisos necesarios han sido concedidos
            } else {
                // No todos los permisos fueron concedidos, manejar según la política de la app, posiblemente cerrando la app
                closeAppDueToLackOfPermissions()
            }
        }

        fun closeAppDueToLackOfPermissions() {
            Toast.makeText(this, "Todos los permisos necesarios no fueron concedidos. La aplicación se cerrará.", Toast.LENGTH_LONG).show()
            finish()
        }




    /************************************************************************
     * Esta función se encarga de gestionar el botón "STOP" de la notificación,
     * recoge la información del bundle, el id de la actividad
     * la busca y luego para dicha actividad
     *
    *************************************************************************/

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        var context=this
        Log.d("T", "Se va a activar el botón")
        intent?.getIntExtra("id", -1)?.let { id ->
            if (id != -1) {
                Log.d("T", "El id es $id")
                viewModel.viewModelScope.launch {
                    viewModel.obtenerActividadPorId(id).firstOrNull()?.let { actividad ->
                        if (actividad.isPlaying){
                            viewModel.togglePlay(actividad = actividad, context)
                        }

                    } ?: Log.d("E", "Ha habido un error")
                }
            }
        }
    }


}
/************************************************************************
 * Composable con el esqueleto de la actividad, esta formado por
 * un scaffold y el contenido interior va variando gracias al nav
 * que depende de la ruta modifica el contenido
 *************************************************************************/
@Composable
fun TaskScheduleApp(viewModel: ActivitiesViewModel) {
    val navController = rememberNavController()
    Scaffold(
        topBar = { TaskBar(navController) },
        bottomBar = { TaskDownBar(navController) }
    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding)) {
            NavigationGraph(navController = navController, viewModel = viewModel)
        }
    }
}


/************************************************************************
 * Composable para el action bar, depende en que ruta esté pone el nombre
 * la ubicación, además contiene el botón de settings
 *************************************************************************/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    var text=""
    if (currentRoute.equals("listaActividades")){
        text= stringResource(id = R.string.Lista)
    }
    else if(currentRoute.equals("datePicker")){
        text= stringResource(id = R.string.Estadísticas)
    }
    else if(currentRoute.equals("settings")){
        text= stringResource(id = R.string.settings)
    }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {


                when (currentRoute) {
                    "listaActividades" -> {

                        Icon(Icons.Filled.List, contentDescription = null)
                    }
                    "datePicker" -> {

                        Icon(Icons.Filled.BarChart, contentDescription = null)
                    }
                    "settings" -> {

                        Icon(Icons.Filled.Settings, contentDescription = null)
                    }

                }

                Spacer(Modifier.width(8.dp))

                Text(text)

            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {},
        actions = {
            IconButton(onClick = {navController.navigate("settings"){
                launchSingleTop = true
                restoreState = false
            }}) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }
    )
}

/************************************************************************
 * Barra inferior para navegar entre las estadísticas y la lista de tasks
 * Tiene dos iconos pulsables que hacen cambiar la ruta para el nav
 *************************************************************************/
@Composable
fun TaskDownBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    val items = listOf(
        "listaActividades" to Icons.Filled.List,
        "datePicker" to Icons.Filled.DateRange

    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 8.dp
    ) {
        Row {
            items.forEach { (route, icon) ->
                IconButton(
                    onClick = {if (currentRoute != route) {navController.navigate(route){
                        launchSingleTop = true
                        restoreState = false
                    }}  },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (currentRoute == route)MaterialTheme.colorScheme.onSurface.copy()
                        else MaterialTheme.colorScheme.primary.copy(0.3f)


                    )
                }
            }
        }
    }
}

/************************************************************************
 * Composable con el navhost, el encargado de cambiar el contenido en
 * función de la ruta
 *************************************************************************/
@Composable
fun NavigationGraph(navController: NavHostController, viewModel: ActivitiesViewModel) {

    NavHost(navController = navController, startDestination = "login", Modifier.fillMaxSize()) {
        composable("login"){
            Log.d("d","El usuario es:"+viewModel.obtenerUltUsuario())
            val ultimoUsuario = viewModel.obtenerUltUsuario()
            DisposableEffect(ultimoUsuario) {
                if (ultimoUsuario.isNotEmpty()) {
                    navController.navigate("listaActividades") {
                        popUpTo("login") { inclusive = true }
                    }
                }
                onDispose { }
            }

            // Mostrar la pantalla de login si no hay último usuario
            if (ultimoUsuario.isEmpty()) {
                LoginScreen(viewModel, navController)
            }


        }

        composable("datePicker") {
            if (viewModel.obtenerUltUsuario().equals("")) {
                LoginScreen(viewModel,navController)
            } else {
                val calendarViewModel: CalendarViewModel = hiltViewModel()
                DatePickerComposable(calendarViewModel = calendarViewModel)
            }
        }

        composable("listaActividades") {
            if (viewModel.obtenerUltUsuario().equals("")) {
                LoginScreen(viewModel, navController)
            } else {
                ListaActividadesUI(viewModel)
            }
        }
        composable(route="settings") {

            LanguageAndThemeSelector(actividadesViewModel = viewModel, navController)

        }
    }
}

//una vez al dia
/*
@Composable
fun configurarAlarma(context: Context,activitiesViewModel: ActivitiesViewModel) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, SincronizacionReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    val calendar: Calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, 1)
        set(Calendar.MINUTE, 41)
        set(Calendar.SECOND, 0)
    }

    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
    )
}*/
@SuppressLint("ShortAlarm")
fun configurarAlarma(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, SincronizacionReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    val calendar: Calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
    }
    Log.d("E","---------Alarma creada!!----------")
    // Ajustar el intervalo a 120,000 milisegundos para que la alarma se ejecute cada 2 minutos
    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        //60000, 1 minuto para testear, funciona!
        60000,  // 3horas 3*60*60*1000
        pendingIntent
    )
}

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            configurarAlarma(context)
            Log.d("BootReceiver", "Alarmas reestablecidas después del reinicio.")
        }
    }
}