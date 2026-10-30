package com.example.monitit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

/* =========================================================
   DASHBOARD
   Equipos funcionando / críticos (datos reales, solo ICMP).
   Usa StatusCard, AvailabilityCard y AlertItem que ya existen en main.
   ========================================================= */

private fun porcentaje(parte: Int, total: Int): String =
    if (total > 0) "${parte * 100 / total}% de $total equipos" else "Sin equipos"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {

    val state by viewModel.state.collectAsState()

    // Carga al entrar y repite cada REFRESH_MS mientras la pestaña está visible
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.refresh()
            delay(ZabbixConfig.REFRESH_MS)
        }
    }

    val stats = state.stats

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Column {
                        Text(
                            text = "MONITITZ",
                            color = Color(0xFF172B4D),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Monitoreo de infraestructura",
                            color = Color(0xFF172B4D),
                            fontSize = 12.sp
                        )
                    }
                },

                actions = {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificaciones",
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }

    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Dashboard",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172B4D)
                )

                Text(
                    text = "Resumen general de la infraestructura",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                state.lastUpdate?.let { hora ->
                    Text(
                        text = "Según ICMP • actualizado $hora",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            /* ---------- Error ---------- */

            state.error?.let { mensaje ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEC))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = mensaje, fontSize = 13.sp, color = Color(0xFFB91C1C))
                            if (stats != null) {
                                Text(
                                    text = "Mostrando la última información disponible.",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            /* ---------- Equipos funcionando / críticos ---------- */

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    StatusCard(
                        modifier = Modifier.weight(1f),
                        title = "Equipos\nfuncionando",
                        value = stats?.working?.toString() ?: "—",
                        subtitle = if (stats != null) porcentaje(stats.working, stats.total)
                        else "Cargando...",
                        icon = Icons.Default.CheckCircle,
                        iconColor = Color(0xFF16A34A)
                    )

                    StatusCard(
                        modifier = Modifier.weight(1f),
                        title = "Equipos\ncríticos",
                        value = stats?.critical?.toString() ?: "—",
                        subtitle = if (stats != null) "Sin respuesta ICMP"
                        else "Cargando...",
                        icon = Icons.Default.Error,
                        iconColor = Color(0xFFDC2626)
                    )
                }
            }


            /* ---------- Disponibilidad (aún de ejemplo) ---------- */

            item {
                Text(
                    text = "Disponibilidad",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172B4D)
                )

                Text(
                    text = "Últimos 7 días",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                AvailabilityCard()
            }


            /* ---------- Últimas alertas (aún de ejemplo) ---------- */

            item {
                Text(
                    text = "Últimas alertas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172B4D)
                )

                Spacer(modifier = Modifier.height(8.dp))

                AlertItem(
                    color = Color(0xFFDC2626),
                    title = "Servidor BD01 no responde",
                    date = "24/08/2026 10:18 AM"
                )

                AlertItem(
                    color = Color(0xFFF59E0B),
                    title = "Uso de CPU alto en SRV-APP",
                    date = "24/08/2026 09:52 AM"
                )

                AlertItem(
                    color = Color(0xFFF59E0B),
                    title = "Switch piso 2 sin conectividad",
                    date = "24/08/2026 09:41 AM"
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
