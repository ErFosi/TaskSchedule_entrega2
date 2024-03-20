package com.example.taskschedule.screens

import android.app.DatePickerDialog
import android.graphics.Canvas
import android.graphics.Rect
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.util.Calendar
import kotlin.math.ceil
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.material.ContentAlpha
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.MaterialTheme.colors
import com.example.taskschedule.data.Actividad
import com.example.taskschedule.viewmodels.CalendarViewModel
import com.github.tehras.charts.bar.BarChart
import com.github.tehras.charts.bar.BarChartData
import com.github.tehras.charts.bar.renderer.label.SimpleValueDrawer
import com.github.tehras.charts.bar.renderer.xaxis.SimpleXAxisDrawer
import com.github.tehras.charts.bar.renderer.xaxis.XAxisDrawer
import com.github.tehras.charts.bar.renderer.yaxis.SimpleYAxisDrawer
import com.github.tehras.charts.bar.renderer.yaxis.YAxisDrawer
import com.github.tehras.charts.piechart.AxisLine
import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.PieChartData
import com.github.tehras.charts.piechart.renderer.SimpleSliceDrawer
import java.text.SimpleDateFormat
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.ArrayList
import java.util.Locale
import kotlin.random.Random
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import android.content.Intent
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import com.example.taskschedule.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/************************************************************************
 * Composable que contiene toda la parte de la interfaz referente a la
 * selección del día y contiene la estructura de toda la página combinando
 * el gráfico, la leyenda y demás composables de esta sección.
 *************************************************************************/
@Composable
fun DatePickerComposable(calendarViewModel: CalendarViewModel) {
    val context = LocalContext.current
    val fechaSelec by calendarViewModel.fechaSelec.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val lista by calendarViewModel.actividadesFecha.collectAsState(initial = emptyList())
    var mostrarGraficoCircular by rememberSaveable { mutableStateOf(true) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var agrupacionCategoria by rememberSaveable { mutableStateOf(false) }
    val colorSeleccionado = MaterialTheme.colors.primary
    val colorNoSeleccionado = MaterialTheme.colors.secondaryVariant
    Column(modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Row(
            modifier = Modifier
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val stringF=stringResource(id = R.string.FechaSelec)
            IconButton(onClick = { val nuevaFecha = fechaSelec.minusDays(1)
                calendarViewModel.cambioFecha(nuevaFecha)
                Toast.makeText(context, stringF+"${nuevaFecha.format(formatter)}", Toast.LENGTH_SHORT).show()}) {
                Icon(Icons.Default.ArrowLeft, contentDescription = stringResource(id = R.string.Decrementar))
            }


            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            ) {

                TextButton(onClick = {
                    DatePickerDialog(context, { _, year, month, dayOfMonth ->
                        val fechaNueva = LocalDate.of(year, month + 1, dayOfMonth)
                        calendarViewModel.cambioFecha(fechaNueva)
                        Toast.makeText(context, stringF+"${fechaNueva.format(formatter)}", Toast.LENGTH_SHORT).show()
                    }, fechaSelec.year, fechaSelec.monthValue - 1, fechaSelec.dayOfMonth).show()
                }) {
                    Text(stringF+"${fechaSelec.format(formatter)}",textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    //Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar fecha")
                }
            }


            IconButton(onClick = { val fechaNueva = fechaSelec.plusDays(1)
                calendarViewModel.cambioFecha(fechaNueva)
                Toast.makeText(context, stringF+" ${fechaNueva.format(formatter)}", Toast.LENGTH_SHORT).show()}) {
                Icon(Icons.Default.ArrowRight, contentDescription = stringResource(id = R.string.Decrementar))
            }
        }

        Divider()
        Spacer(modifier = Modifier.height(20.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(id = R.string.Graf), textAlign = TextAlign.Center,style = MaterialTheme.typography.title1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { mostrarGraficoCircular = true }) {
                    Icon(
                        Icons.Default.PieChart,
                        contentDescription = stringResource(id = R.string.Circ),
                        tint = if (mostrarGraficoCircular) colorSeleccionado else colorNoSeleccionado
                    )
                }





                IconButton(onClick = { mostrarGraficoCircular = false }) {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = stringResource(id = R.string.Bar),
                        tint = if (!mostrarGraficoCircular) colorSeleccionado else colorNoSeleccionado
                    )
                }
                Spacer(Modifier.width(8.dp))


                Text(text = stringResource(id = R.string.Agrupado))


                var textoElecc = ""
                if (agrupacionCategoria) {
                    textoElecc = stringResource(id = R.string.Categ)
                } else {
                    textoElecc = stringResource(id = R.string.Act)
                }
                Box {
                    Row( modifier = Modifier
                        .padding(1.dp)
                        .clickable(onClick = { expanded = true }),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Text(
                            text = textoElecc,
                            modifier = Modifier
                                .padding(8.dp)
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = stringResource(id = R.string.Selector),
                            tint = if (mostrarGraficoCircular) colorSeleccionado else colorNoSeleccionado
                        )

                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem( text = { Text(stringResource(id = R.string.Act)) },onClick = {
                            agrupacionCategoria = false
                            expanded = false
                        })
                        DropdownMenuItem(text = { Text(stringResource(id = R.string.Categ)) },onClick = {
                            agrupacionCategoria = true
                            expanded = false
                        })
                    }
                }
            }
        }
        if(mostrarGraficoCircular){
            Tarta(agruparPorCategoria(lista,agrupacionCategoria))
        }
        else{
            Barras(agruparPorCategoria(lista,agrupacionCategoria))
        }

        Spacer(modifier = Modifier.height(35.dp))


        SaveAsJSONSection(calendarViewModel = calendarViewModel)



        Spacer(modifier = Modifier.height(35.dp))

        var text="Nothing"
        var strings:MutableList<String> = mutableListOf()
        strings.add(stringResource(id = R.string.Invertido))
        strings.add(stringResource(id = R.string.Manera))
        if (lista.isEmpty()){

        }

        Button(

            onClick = {
                if (!lista.isEmpty()) {
                    calendarViewModel.viewModelScope.launch {
                        val resumen = StringBuilder()
                        val fecha=fechaSelec.format(DateTimeFormatter.ISO_DATE)
                        resumen.append(strings[0] + " $fecha" +" "+ strings[1] + "\n")
                        resumen.append("\n")
                             lista.forEach(){ act ->


                                 resumen.append("${act.nombre} -> ${formatTime(act.tiempo) } \n")

                            }
                        resumen.append("\n")
                        text=resumen.toString()

                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, text)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }
                }
            },

            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .height(40.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(id = R.string.Compartir),style = MaterialTheme.typography.body1)
                
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Share, contentDescription = stringResource(id = R.string.Compartir))
            }
        }




    }




}

/************************************************************************
 * Esta función devuelve una lista de actividades que cuando agrupacionCategoria
 * esta activo devuelve una actividad por categoría con la suma
 * de los tiempos de cada categoría, si no la deja como está
 *************************************************************************/


fun agruparPorCategoria(lista: List<Actividad>, agrupacionCategoria: Boolean): List<Actividad> {
    return if (agrupacionCategoria) {

        lista.groupBy { it.categoria }

            .map { entry ->
                Actividad(
                    nombre = entry.key,
                    tiempo = entry.value.sumOf { it.tiempo },
                    id = 1
                )
            }
    } else {

        lista
    }
}
/************************************************************************
 * Composable en cargo de la elaboración del gráfico de barras
 *************************************************************************/
@Composable
fun Barras(lista: List<Actividad>) {
    var barras=ArrayList<BarChartData.Bar>()
    when {
        lista.isEmpty() -> {
            Text(text = stringResource(id = R.string.No_Data))
        }

        else -> {
            val lista_ord=lista.sortedBy{ it.tiempo }.asReversed()
            var col:Color
            var tiempoSobrante=0
            lista_ord.mapIndexed { index, act ->
                if(index>colores.size-1){
                    tiempoSobrante=tiempoSobrante+act.tiempo
                }
                else{
                    var tiempo =act.tiempo
                    if (act.tiempo<1){
                        tiempo=1
                    }
                    col=colores[index]
                    barras.add(BarChartData.Bar(label = "", value = tiempo.toFloat(), color = col))
                }

            }
            if (tiempoSobrante>0){
                barras.add(BarChartData.Bar(label = "", value = tiempoSobrante.toFloat(), color = Color.Gray))
            }


            BarChart(barChartData = BarChartData(bars = barras),modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .height(220.dp),  yAxisDrawer = SimpleYAxisDrawer(labelValueFormatter = ::formatTimeFloat, axisLineThickness = 0.dp  ), labelDrawer = SimpleValueDrawer(drawLocation = SimpleValueDrawer.DrawLocation.Outside)
            )
            Spacer(modifier = Modifier.height(16.dp))

            LeyendaMatriz(lista = lista)
        }
    }
}
/************************************************************************
 * Composable que se encarga del gráfico circular
 *************************************************************************/
@Composable
fun Tarta(lista : List<Actividad>) {
    var slices = ArrayList<PieChartData.Slice>()
    when {
        lista.isEmpty() -> {
            Text(text = stringResource(id = R.string.No_Data))
        }

        else -> {
            lista.mapIndexed { index, act ->
                var tiempo =act.tiempo
                if(tiempo<1){
                    tiempo=1
                }
                var col : Color
                if(index>colores.size-1){
                    col=Color.Gray
                }
                else{
                    col=colores[index]
                }
                slices.add(
                    PieChartData.Slice(
                        value = tiempo.toFloat(),
                        color = col
                    )
                )
            }

            PieChart(
                pieChartData = PieChartData(slices),
                modifier = Modifier
                    .padding(horizontal = 15.dp, vertical = 20.dp)
                    .height(220.dp),
                sliceDrawer = SimpleSliceDrawer(sliceThickness = 100f)

            )
            Log.d("p","Tarta creada:"+slices.size)
            Spacer(modifier = Modifier.height(16.dp))

            LeyendaMatriz(lista = lista)
        }
    }
}
/************************************************************************
 * Lista de colores usados en los graficos
 *************************************************************************/
var colores = mutableListOf(
    Color(0xFF4CAF50), // Verde
    Color(0xFFFFC107), // Ámbar
    Color(0xFFE91E63), // Rosa
    Color(0xFF9C27B0), // Púrpura
    Color(0xFFFF5722), // Naranja oscuro
    Color(0xFF03A9F4), // Azul claro
    Color(0xFF009688), // Verde azulado
    Color(0xFFFFEB3B), // Amarillo
    //Color(0xFF673AB7), // Púrpura oscuro
    //Color(0xFF8BC34A), // Verde claro
    //Color(0xFFCDDC39), // Lima
    //Color(0xFFFF9800), // Naranja
    //Color(0xFF00BCD4), // Cian
    //Color(0xFF2196F3), // Azul
    //Color(0xFF3F51B5), // Índigo
    //Color(0xFF795548), // Marrón
    //Color(0xFF9E9E9E), // Gris
    Color(0xFF607D8B)  // Azul grisáceo
)

/************************************************************************
 * Composable que contiene un item de la leyenda
 *************************************************************************/
@Composable
fun LeyendaItem(nombre: String, color: Color) {
    var expanded by remember { mutableStateOf(false) }


    Box(modifier = Modifier.clickable { expanded = true }) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .width(100.dp)
            .padding(start = 10.dp)) {
            Box(
                modifier = Modifier
                    .size(15.dp)
                    .background(color = color, shape = RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = nombre,
                modifier = Modifier
                    .padding(end = 1.dp)
                    .widthIn(max = 60.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }


        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier

        ) {
            Text(
                text = nombre,
                modifier = Modifier
                    .padding(4.dp)
                    .heightIn(max = 32.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
/************************************************************************
 * Composable que crea una matriz de filas de 3 items de leyendas del
 * composable anterior
 *************************************************************************/
@Composable
fun LeyendaMatriz(lista: List<Actividad>) {

    val numRows = (lista.size + 3) / 3

    Column(Modifier.height(85.dp)) {
        var exit=false
        for (row in 0 until numRows) {
            if (exit){
                break
            }

            Row {
                for (column in 0 until 3) {
                    if (exit){
                        break
                    }
                    val index = row * 3 + column
                    if (index < lista.size) {
                        val act = lista[index]
                        val col: Color = if (index >= colores.size) {
                            LeyendaItem(nombre = stringResource(id = R.string.otros), color = Color.Gray)
                            exit=true
                            break
                        } else {
                            colores[index]
                        }
                        LeyendaItem(nombre = act.nombre, color = col)
                    }
                }
            }
        }
    }
}


/************************************************************************
 * Botón encargado del botón de descargar los datos en un fichero de texto
 * con formato json, tiene la parte de la logica en la primera parte y la
 * interfaz al final
 *************************************************************************/
@Composable
private fun SaveAsJSONSection(calendarViewModel: CalendarViewModel) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val fechaSelec by calendarViewModel.fechaSelec.collectAsState()
    val filename = "activities_"+ "${fechaSelec.format(formatter)}"+".txt"
    val contentResolver = LocalContext.current.contentResolver
    val saverLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument(
        "application/text"
    )
    ) { uri ->
        if (uri != null) {
            try {
                contentResolver.openFileDescriptor(uri, "w")?.use {
                    FileOutputStream(it.fileDescriptor).use { fileOutputStream ->
                        fileOutputStream.write(
                            (calendarViewModel.descargarActividadesJson()).toByteArray()
                        )
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    Button(
        onClick = { saverLauncher.launch(filename) },
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .height(48.dp)
    ) {
        Text(stringResource(id = R.string.Descargar),style = MaterialTheme.typography.body1)
    }


}

