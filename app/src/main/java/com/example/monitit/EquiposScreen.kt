package com.example.monitit


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.TextStyle



/* =========================================================
   EQUIPOS  (datos reales de Zabbix: Monitorización -> Equipos)
   ========================================================= */

@Composable
fun EquiposScreen(
    viewModel: EquiposViewModel = viewModel()
) {

    val state by viewModel.state.collectAsState()

    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(0) }   // 0 todos, 1 con problemas, 2 deshabilitados

    // Carga al entrar y repite cada REFRESH_MS mientras la pestaña está visible
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.refresh()
            delay(ZabbixConfig.REFRESH_MS)
        }
    }

    val visibles = remember(state.hosts, query, filter) {
        val q = query.trim()
        state.hosts.filter { h ->
            val coincide = q.isEmpty() ||
                    h.name.contains(q, ignoreCase = true) ||
                    h.address.contains(q, ignoreCase = true)

            val pasaFiltro = when (filter) {
                1 -> h.totalProblems > 0
                2 -> !h.enabled
                else -> true
            }
            coincide && pasaFiltro
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(horizontal = 16.dp)
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        /* ---------- Encabezado ---------- */

        Row(verticalAlignment = Alignment.CenterVertically) {

            Icon(
                imageVector = Icons.Default.Computer,
                contentDescription = null,
                tint = Color(0xFF123B68),
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Equipos",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172B4D)
                )
                Text(
                    text = "Monitorización • Equipos • Zabbix",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            IconButton(onClick = { viewModel.refresh() }) {
                if (state.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar",
                        tint = Color(0xFF123B68)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        /* ---------- Resumen ---------- */

        state.lastUpdate?.let { hora ->
            val conProblemas = state.hosts.count { it.totalProblems > 0 }
            Text(
                text = "${state.hosts.size} equipos • $conProblemas con problemas • actualizado $hora",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        /* ---------- Error ---------- */

        state.error?.let { mensaje ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEC))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = mensaje, fontSize = 13.sp, color = Color(0xFFB91C1C))
                    if (state.hosts.isNotEmpty()) {
                        Text(
                            text = "Mostrando la última información disponible.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        /* ---------- Búsqueda y filtros ---------- */

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = {
                Text(
                    "Buscar por nombre o IP",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            },
            singleLine = true,
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 14.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF123B68),
                unfocusedBorderColor = Color(0xFFD1D5DB),
                cursorColor = Color(0xFF123B68)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterPill("Todos", filter == 0) { filter = 0 }
            FilterPill("Con problemas", filter == 1) { filter = 1 }
            FilterPill("Deshabilitados", filter == 2) { filter = 2 }
        }

        Spacer(modifier = Modifier.height(12.dp))

        /* ---------- Contenido ---------- */

        when {

            // Primera carga
            state.hosts.isEmpty() && state.loading && state.lastUpdate == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Conectó bien pero no hay resultados
            visibles.isEmpty() && state.lastUpdate != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.hosts.isEmpty()) "No hay equipos configurados"
                        else "Ningún equipo coincide con el filtro",
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(visibles, key = { it.hostId }) { host ->
                        HostCard(host)
                    }
                }
            }
        }
    }
}


/* =========================================================
   CHIP DE FILTRO
   ========================================================= */

@Composable
private fun FilterPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) Color.White else Color(0xFF374151),
        modifier = Modifier
            .background(
                if (selected) Color(0xFF123B68) else Color(0xFFE5E7EB),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}


/* =========================================================
   TARJETA DE EQUIPO
   ========================================================= */

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HostCard(h: HostUi) {

    // Color de la barra lateral: gris si está deshabilitado,
    // color de la peor gravedad si tiene problemas, verde si está OK
    val barColor = when {
        !h.enabled -> Color(0xFF9CA3AF)
        h.worstSeverity != null -> Color(h.worstSeverity!!.color)
        else -> Color(0xFF16A34A)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        Row(modifier = Modifier.height(IntrinsicSize.Min)) {

            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(barColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {

                // Estado + disponibilidad (ZBX / SNMP / IPMI / JMX)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    val (estadoTxt, estadoColor) = when {
                        !h.enabled -> "DESHABILITADO" to Color(0xFF6B7280)
                        h.inMaintenance -> "MANTENIMIENTO" to Color(0xFFF59E0B)
                        else -> "HABILITADO" to Color(0xFF16A34A)
                    }

                    Text(
                        text = estadoTxt,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = estadoColor
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        h.availability.forEach { a ->
                            val bg = when (a.status) {
                                1 -> Color(0xFF16A34A)
                                2 -> Color(0xFFDC2626)
                                else -> Color(0xFF97AAB3)
                            }
                            Text(
                                text = a.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .background(bg, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Nombre
                Text(
                    text = h.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Interfaz (IP:puerto)
                Text(
                    text = h.address,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Problemas por gravedad (de más grave a menos grave)
                if (h.problems.isEmpty()) {
                    Text(
                        text = "Sin problemas",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Problemas",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        h.problems.entries
                            .sortedByDescending { it.key.ordinal }
                            .forEach { (sev, count) ->
                                Text(
                                    text = count.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sev.darkText) Color(0xFF1F2937) else Color.White,
                                    modifier = Modifier
                                        .background(Color(sev.color), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                    }
                }

                // Etiquetas
                if (h.tags.isNotEmpty()) {

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        h.tags.forEach { tag ->
                            Text(
                                text = tag,
                                fontSize = 10.sp,
                                color = Color(0xFF374151),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .background(Color(0xFFE5E7EB), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
