package com.example.taskschedule.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalGlanceId
import androidx.glance.appwidget.GlanceAppWidget

import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taskschedule.data.Actividad
import com.example.taskschedule.repositories.ActividadesRepository
import com.example.taskschedule.screens.formatTime
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import java.time.LocalDate


import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Spacer
import androidx.glance.text.FontFamily
import androidx.glance.text.FontStyle
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext


class Widget(private var actividades: List<Actividad> = emptyList()) : GlanceAppWidget() {
    fun updateActividades(newActividades: List<Actividad>) {
        actividades = newActividades.sortedByDescending { it.tiempo }.take(5)
        Log.d("Widget", "Se actualiza el widget")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(color = Color(0xFF253177))
                    .padding(8.dp)
            ) {
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(
                        text = "Top Tasks",
                        modifier = GlanceModifier.fillMaxWidth().padding(2.dp),
                        style = androidx.glance.text.TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            fontStyle = FontStyle.Normal,
                            color = ColorProvider(Color.LightGray),
                            fontFamily = FontFamily.SansSerif
                        ),

                    )
                    Spacer(modifier = GlanceModifier.height(3.dp))
                    actividades.forEach { actividad ->
                        Spacer(modifier = GlanceModifier.height(3.dp))
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 3.dp)
                                .background(day = Color(0xA94266D1), night = Color(0xA94266D1))
                                .cornerRadius(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = actividad.nombre,
                                modifier = GlanceModifier.padding(horizontal = 3.dp, vertical =2.dp),
                                style = androidx.glance.text.TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(Color.LightGray)
                                )
                            )
                            Text(
                                text = formatTime(actividad.tiempo),
                                modifier = GlanceModifier.padding(horizontal = 3.dp, vertical = 2.dp),
                                style = androidx.glance.text.TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(Color.LightGray)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetReceiverEntryPoint {
    fun actividadesRepository(): ActividadesRepository
}
class MyAppWidgetReceiver : GlanceAppWidgetReceiver() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    override val glanceAppWidget: GlanceAppWidget = Widget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val appComponent = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetReceiverEntryPoint::class.java
        )
        val actividadesRepo = appComponent.actividadesRepository()
        Log.d("Widget","Entra al widget receiver")

        serviceScope.launch {
            try {
                Log.d("E","Intento de obtener acts")
                val actividades = actividadesRepo.getActividadesPorFecha(LocalDate.now()).first()
                Log.d("WidgetUpdate", "Actividades recuperadas: ${actividades.size}")
                (glanceAppWidget as Widget).updateActividades(actividades)
                (glanceAppWidget as Widget).updateAll(context)
            } catch (e: Exception) {
                Log.e("WidgetError", "Error al recuperar actividades: ${e.message}")
            }
        }
    }

}

class AppCloseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_SHUTDOWN.equals(intent.action)) {
            val widgetUpdateIntent = Intent(context, MyAppWidgetReceiver::class.java)
            widgetUpdateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, Widget::class.java))
            widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(widgetUpdateIntent)
        }
    }
}
