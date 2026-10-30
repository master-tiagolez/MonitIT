package com.example.monitit

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

/* =========================================================
   ALERTAS  (datos reales de Zabbix: Problemas recientes)
   ========================================================= */

@Composable
fun AlertasScreen(
    viewModel: AlertasViewModel = viewModel()
) {

    val state by viewModel.state.collectAsState()

    // Se ejecuta solo mientras la pestaña Alertas está visible:
    // carga al entrar y luego repite cada REFRESH_MS.
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.refresh()
            delay(ZabbixConfig.REFRESH_MS)
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
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color(0xFF123B68),
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = "Alertas",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172B4D)
                )

                Text(
                    text = "Problemas recientes • Zabbix",
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
            val activos = state.problems.count { !it.resolved }
            Text(
                text = "$activos activos • actualizado $hora",
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
                    Text(
                        text = mensaje,
                        fontSize = 13.sp,
                        color = Color(0xFFB91C1C)
                    )
                    if (state.problems.isNotEmpty()) {
                        Text(
                            text = "Mostrando la última información disponible.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        /* ---------- Contenido ---------- */

        when {

            // Primera carga
            state.problems.isEmpty() && state.loading && state.lastUpdate == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Conectó bien pero no hay problemas
            state.problems.isEmpty() && state.lastUpdate != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin problemas recientes",
                        fontSize = 16.sp,
                        color = Color(0xFF16A34A),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.problems, key = { it.eventId }) { problem ->
                        ProblemCard(problem)
                    }
                }
            }
        }
    }
}


/* =========================================================
   TARJETA DE PROBLEMA
   ========================================================= */

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProblemCard(p: ProblemUi) {

    val severityColor = Color(p.severity.color)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        Row(modifier = Modifier.height(IntrinsicSize.Min)) {

            // Barra lateral con el color de la gravedad
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(severityColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {

                // Gravedad + hora
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .background(severityColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = p.severity.label.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (p.severity.darkText) Color(0xFF1F2937) else Color.White
                        )
                    }

                    Text(
                        text = p.time,
                        fontSize = 12.sp,
                        color = Color(0xFF3B82F6)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Equipo
                Text(
                    text = p.host,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Problema
                Text(
                    text = p.problem,
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Estado • duración • reconocida
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        text = if (p.resolved) "RESUELTO" else "PROBLEMA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (p.resolved) Color(0xFF16A34A) else Color(0xFFDC2626)
                    )

                    Text(
                        text = "  •  ${p.duration}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    if (p.acknowledged) {
                        Text(
                            text = "  •  Reconocida",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Etiquetas
                if (p.tags.isNotEmpty()) {

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        p.tags.forEach { tag ->
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
