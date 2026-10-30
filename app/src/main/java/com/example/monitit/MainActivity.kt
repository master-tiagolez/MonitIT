package com.example.monitit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monitit.ui.theme.MonititTheme
import androidx.compose.material3.NavigationBarItemDefaults



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MonititTheme {
                MonititApp()
            }
        }
    }
}


/* =========================================================
   APLICACIÓN PRINCIPAL
   ========================================================= */

@Composable
fun MonititApp() {

    var selectedScreen by remember {
        mutableStateOf(0)
    }

    Scaffold(

        bottomBar = {

            BottomNavigationBar(
                selectedScreen = selectedScreen,
                onScreenSelected = {
                    selectedScreen = it
                }
            )
        }

    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            when (selectedScreen) {

                0 -> DashboardScreen()

                1 -> EquiposScreen() // antes 1 -> MonitoreoScreen()

                2 -> AlertasScreen()

                3 -> ReportesScreen()

                4 -> MasScreen()
            }
        }
    }
}


/* =========================================================
   BARRA DE NAVEGACIÓN INFERIOR
   ========================================================= */

@Composable
fun BottomNavigationBar(
    selectedScreen: Int,
    onScreenSelected: (Int) -> Unit
) {

    NavigationBar(
        containerColor = Color.White
    ) {

        NavigationBarItem(

            selected = selectedScreen == 0,

            onClick = {
                onScreenSelected(0)
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Dashboard"
                )
            },

            label = {
                Text("Dashboard")
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                unselectedIconColor = Color.Black,
                selectedTextColor = Color.Black,
                unselectedTextColor = Color.Black,
                indicatorColor = Color.Transparent
            )
        )


        NavigationBarItem(

            selected = selectedScreen == 1,

            onClick = {
                onScreenSelected(1)
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.Computer,
                    contentDescription = "Monitoreo"
                )
            },

            label = {
                Text("Equipos")
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                unselectedIconColor = Color.Black,
                selectedTextColor = Color.Black,
                unselectedTextColor = Color.Black,
                indicatorColor = Color.Transparent
            )
        )


        NavigationBarItem(

            selected = selectedScreen == 2,

            onClick = {
                onScreenSelected(2)
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Alertas"
                )
            },

            label = {
                Text("Problemas")
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                unselectedIconColor = Color.Black,
                selectedTextColor = Color.Black,
                unselectedTextColor = Color.Black,
                indicatorColor = Color.Transparent
            )
        )


        NavigationBarItem(

            selected = selectedScreen == 3,

            onClick = {
                onScreenSelected(3)
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Reportes"
                )
            },

            label = {
                Text("Informes")
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                unselectedIconColor = Color.Black,
                selectedTextColor = Color.Black,
                unselectedTextColor = Color.Black,
                indicatorColor = Color.Transparent
            )
        )


        NavigationBarItem(

            selected = selectedScreen == 4,

            onClick = {
                onScreenSelected(4)
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Más"
                )
            },

            label = {
                Text("Más")
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                unselectedIconColor = Color.Black,
                selectedTextColor = Color.Black,
                unselectedTextColor = Color.Black,
                indicatorColor = Color.Transparent
            )
        )
    }
}


/* =========================================================
   DASHBOARD ANTIGUO SIN DATOS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Image(
                            painter = painterResource(id = R.drawable.monitit_logo),
                            contentDescription = "MONITIT Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {

                            Text(
                                text = "MONITIT",
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

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

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
            }


            item {

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    StatusCard(

                        modifier = Modifier.weight(1f),

                        title = "Equipos\nfuncionando",

                        value = "45",

                        subtitle = "64% del total",

                        icon = Icons.Default.CheckCircle,

                        iconColor = Color(0xFF16A34A)
                    )


                    StatusCard(

                        modifier = Modifier.weight(1f),

                        title = "Advertencias",

                        value = "8",

                        subtitle = "11% del total",

                        icon = Icons.Default.Warning,

                        iconColor = Color(0xFFF59E0B)
                    )
                }
            }


            item {

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    StatusCard(

                        modifier = Modifier.weight(1f),

                        title = "Problemas\ncríticos",

                        value = "5",

                        subtitle = "7% del total",

                        icon = Icons.Default.Error,

                        iconColor = Color(0xFFDC2626)
                    )


                    StatusCard(

                        modifier = Modifier.weight(1f),

                        title = "Total\ndispositivos",

                        value = "58",

                        subtitle = "100%",

                        icon = Icons.Default.Devices,

                        iconColor = Color(0xFF334155)
                    )
                }
            }


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

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                AvailabilityCard()
            }


            item {

                Text(
                    text = "Últimas alertas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172B4D)
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

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

                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }
        }
    }
}========================================================= */




/* =========================================================
   TARJETAS DEL DASHBOARD
   ========================================================= */

@Composable
fun StatusCard(

    modifier: Modifier = Modifier,

    title: String,

    value: String,

    subtitle: String,

    icon: ImageVector,

    iconColor: Color

) {

    Card(

        modifier = modifier,

        shape = RoundedCornerShape(12.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(

            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(

                    imageVector = icon,

                    contentDescription = null,

                    tint = iconColor,

                    modifier = Modifier.size(24.dp)
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(

                    text = title,

                    fontSize = 13.sp,

                    fontWeight = FontWeight.Medium,

                    color = Color(0xFF475569)
                )
            }


            Spacer(
                modifier = Modifier.height(8.dp)
            )


            Text(

                text = value,

                fontSize = 28.sp,

                fontWeight = FontWeight.Bold,

                color = Color(0xFF172B4D)
            )


            Text(

                text = subtitle,

                fontSize = 11.sp,

                color = Color.Gray
            )
        }
    }
}


/* =========================================================
   DISPONIBILIDAD
   ========================================================= */

@Composable
fun AvailabilityCard() {

    Card(

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(12.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "Disponibilidad general",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )


            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,

                verticalAlignment = Alignment.Bottom
            ) {

                AvailabilityBar("18/08", 0.85f)

                AvailabilityBar("19/08", 0.78f)

                AvailabilityBar("20/08", 0.62f)

                AvailabilityBar("21/08", 0.80f)

                AvailabilityBar("22/08", 0.67f)

                AvailabilityBar("23/08", 0.84f)

                AvailabilityBar("24/08", 0.76f)
            }
        }
    }
}


@Composable
fun AvailabilityBar(

    date: String,

    percentage: Float

) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(

            modifier = Modifier
                .width(24.dp)
                .height(100.dp),

            contentAlignment = Alignment.BottomCenter
        ) {

            Box(

                modifier = Modifier
                    .width(18.dp)
                    .height((100 * percentage).dp)
                    .background(
                        color = Color(0xFF2563EB),
                        shape = RoundedCornerShape(
                            topStart = 4.dp,
                            topEnd = 4.dp
                        )
                    )
            )
        }


        Spacer(
            modifier = Modifier.height(4.dp)
        )


        Text(
            text = date,
            fontSize = 9.sp,
            color = Color.Gray
        )
    }
}


/* =========================================================
   ALERTA
   ========================================================= */

@Composable
fun AlertItem(

    color: Color,

    title: String,

    date: String

) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),

        shape = RoundedCornerShape(10.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(

                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(50)
                    )
            )


            Spacer(
                modifier = Modifier.width(12.dp)
            )


            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(

                    text = title,

                    fontSize = 14.sp,

                    fontWeight = FontWeight.Medium,

                    color = Color(0xFF172B4D)
                )


                Text(

                    text = date,

                    fontSize = 11.sp,

                    color = Color.Gray
                )
            }


            Text(

                text = "›",

                fontSize = 24.sp,

                color = Color.Gray
            )
        }
    }
}


/* =========================================================
   MONITOREO
   ========================================================= */

@Composable
fun MonitoreoScreen() {

    SimpleScreen(

        title = "Monitoreo",

        subtitle = "Lista de dispositivos monitoreados",

        icon = Icons.Default.Computer
    ) {

        DeviceCard(
            name = "SRV-APP",
            ip = "192.168.1.10",
            type = "Servidor",
            status = "OK"
        )

        DeviceCard(
            name = "SRV-BD01",
            ip = "192.168.1.11",
            type = "Servidor",
            status = "CRÍTICO"
        )

        DeviceCard(
            name = "PC-ADMIN-01",
            ip = "192.168.1.2",
            type = "Estación",
            status = "OK"
        )

        DeviceCard(
            name = "SW-PISO2",
            ip = "192.168.1.5",
            type = "Switch",
            status = "ADVERTENCIA"
        )

        DeviceCard(
            name = "RTR-PRINCIPAL",
            ip = "192.168.1.1",
            type = "Router",
            status = "OK"
        )

    }
}





/* =========================================================
   REPORTES
   ========================================================= */

@Composable
fun ReportesScreen() {

    SimpleScreen(

        title = "Reportes",

        subtitle = "Generación y consulta de reportes",

        icon = Icons.Default.Description
    ) {

        ReportCard(
            title = "Disponibilidad",
            description = "Reporte de disponibilidad de dispositivos."
        )

        ReportCard(
            title = "Incidentes",
            description = "Reporte de incidentes y alertas generadas."
        )

        ReportCard(
            title = "Rendimiento",
            description = "Reporte de rendimiento de recursos."
        )

        ReportCard(
            title = "Personalizado",
            description = "Crea reportes según tus necesidades."
        )
    }
}


/* =========================================================
   MÁS
   ========================================================= */

@Composable
fun MasScreen() {

    SimpleScreen(

        title = "Más",

        subtitle = "Configuración del sistema",

        icon = Icons.Default.Settings
    ) {

        SettingItem(
            icon = Icons.Default.Person,
            title = "Usuarios",
            description = "Gestión de los usuarios del sistema."
        )

        SettingItem(
            icon = Icons.Default.Security,
            title = "Roles y permisos",
            description = "Administrar roles y permisos de acceso."
        )

        SettingItem(
            icon = Icons.Default.Devices,
            title = "Dispositivos monitoreados",
            description = "Configura los dispositivos que se monitorean."
        )

        SettingItem(
            icon = Icons.Default.Settings,
            title = "Parámetros del sistema",
            description = "Ajusta los parámetros generales."
        )

        SettingItem(
            icon = Icons.Default.Notifications,
            title = "Notificaciones",
            description = "Configura métodos y canales de notificación."
        )
    }
}


/* =========================================================
   PANTALLAS GENERALES
   ========================================================= */

@Composable
fun SimpleScreen(

    title: String,

    subtitle: String,

    icon: ImageVector,

    content: @Composable ColumnScope.() -> Unit

) {

    Column(

        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp)
    ) {

        Spacer(
            modifier = Modifier.height(20.dp)
        )


        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(

                imageVector = icon,

                contentDescription = null,

                tint = Color(0xFF123B68),

                modifier = Modifier.size(30.dp)
            )


            Spacer(
                modifier = Modifier.width(10.dp)
            )


            Column {

                Text(

                    text = title,

                    fontSize = 26.sp,

                    fontWeight = FontWeight.Bold,

                    color = Color(0xFF172B4D)
                )


                Text(

                    text = subtitle,

                    fontSize = 14.sp,

                    color = Color.Gray
                )
            }
        }


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        LazyColumn {

            item {

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    content()
                }
            }
        }
    }
}


/* =========================================================
   TARJETA DE DISPOSITIVO
   ========================================================= */

@Composable
fun DeviceCard(

    name: String,

    ip: String,

    type: String,

    status: String

) {

    Card(

        modifier = Modifier.fillMaxWidth(),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {

                    Text(

                        text = name,

                        fontSize = 17.sp,

                        fontWeight = FontWeight.Bold,

                        color = Color.Black
                    )


                    Text(

                        text = "$ip • $type",

                        fontSize = 12.sp,

                        color = Color.Gray
                    )
                }


                Text(

                    text = status,

                    fontSize = 12.sp,

                    fontWeight = FontWeight.Bold,

                    color = when (status) {

                        "OK" -> Color(0xFF16A34A)

                        "CRÍTICO" -> Color(0xFFDC2626)

                        else -> Color(0xFFF59E0B)
                    }
                )
            }


            Spacer(
                modifier = Modifier.height(10.dp)
            )


            Text(

                text = "CPU 23%     RAM 45%     Almac. 60%",

                fontSize = 11.sp,

                color = Color.Gray
            )
        }
    }
}





/* =========================================================
   TARJETA DE REPORTE
   ========================================================= */

@Composable
fun ReportCard(

    title: String,

    description: String

) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .clickable { },

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(

                text = title,

                fontSize = 17.sp,

                fontWeight = FontWeight.Bold,

                color = Color.Black
            )


            Spacer(
                modifier = Modifier.height(5.dp)
            )


            Text(

                text = description,

                fontSize = 13.sp,

                color = Color.Gray
            )
        }
    }
}


/* =========================================================
   ELEMENTO DE CONFIGURACIÓN
   ========================================================= */

@Composable
fun SettingItem(

    icon: ImageVector,

    title: String,

    description: String

) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .clickable { },

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Row(

            modifier = Modifier.padding(16.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(

                imageVector = icon,

                contentDescription = null,

                tint = Color(0xFF123B68),

                modifier = Modifier.size(28.dp)
            )


            Spacer(
                modifier = Modifier.width(14.dp)
            )


            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(

                    text = title,

                    fontSize = 16.sp,

                    fontWeight = FontWeight.Bold,

                    color = Color.Black
                )


                Text(

                    text = description,

                    fontSize = 12.sp,

                    color = Color.Gray
                )
            }


            Text(

                text = "›",

                fontSize = 24.sp,

                color = Color.Gray
            )
        }
    }
}