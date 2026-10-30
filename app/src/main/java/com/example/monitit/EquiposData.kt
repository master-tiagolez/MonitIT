package com.example.monitit

import com.google.gson.annotations.SerializedName
import kotlin.coroutines.cancellation.CancellationException

/* =========================================================
   MODELOS CRUDOS (host.get / trigger.get)
   ========================================================= */

data class ZbxInterface(
    val interfaceid: String? = null,
    val ip: String? = null,
    val dns: String? = null,
    val port: String? = null,
    val type: String? = null,        // 1=Agente  2=SNMP  3=IPMI  4=JMX
    val main: String? = null,        // "1" = interfaz principal
    val useip: String? = null,       // "1" = conectar por IP, "0" = por DNS
    val available: String? = null    // 0=desconocido  1=disponible  2=no disponible
)

data class ZbxHostFull(
    val hostid: String? = null,
    val name: String? = null,
    val status: String? = null,      // 0=monitoreado  1=deshabilitado
    @SerializedName("maintenance_status") val maintenanceStatus: String? = null,
    val interfaces: List<ZbxInterface>? = null,
    val tags: List<ZbxTag>? = null
)

data class ZbxTrigger(
    val triggerid: String? = null,
    val priority: String? = null,
    val hosts: List<ZbxHost>? = null
)


/* =========================================================
   MODELO PARA LA INTERFAZ
   ========================================================= */

// status: 0 = desconocido (gris), 1 = disponible (verde), 2 = no disponible (rojo)
data class AvailabilityUi(
    val label: String,   // ZBX, SNMP, IPMI, JMX
    val status: Int
)

data class HostUi(
    val hostId: String,
    val name: String,
    val address: String,
    val enabled: Boolean,
    val inMaintenance: Boolean,
    val availability: List<AvailabilityUi>,
    val problems: Map<Severity, Int>,      // problemas activos por gravedad
    val tags: List<String>
) {
    val totalProblems: Int get() = problems.values.sum()
    val worstSeverity: Severity? get() = problems.keys.maxByOrNull { it.ordinal }
}


/* =========================================================
   REPOSITORIO
   Replica "Monitorización -> Equipos" de Zabbix.
   ========================================================= */

class EquiposRepository {

    companion object {
        private const val MAX_HOSTS = 500
    }

    private val api: ZabbixApi
        get() = ZabbixClient.api

    private fun auth(): String? =
        if (ZabbixConfig.USE_BEARER_HEADER) null else ZabbixConfig.API_TOKEN

    suspend fun getHosts(): List<HostUi> {

        // 1) Lista de equipos con interfaces (disponibilidad) y etiquetas
        val hostsResp = api.hostGet(
            RpcRequest(
                method = "host.get",
                params = mapOf(
                    "output" to listOf("hostid", "name", "status", "maintenance_status"),
                    "selectInterfaces" to "extend",
                    "selectTags" to "extend",
                    "sortfield" to listOf("name"),
                    "limit" to MAX_HOSTS
                ),
                auth = auth()
            )
        )

        hostsResp.error?.let { throw ZabbixException(it) }

        val hosts = hostsResp.result.orEmpty()
        if (hosts.isEmpty()) return emptyList()

        // 2) Problemas activos agrupados por equipo y gravedad
        //    (columna "Problemas" de Zabbix)
        val problemsByHost: Map<String, Map<Severity, Int>> = try {
            val resp = api.triggerGet(
                RpcRequest(
                    method = "trigger.get",
                    id = 2,
                    params = mapOf(
                        "output" to listOf("triggerid", "priority"),
                        "selectHosts" to listOf("hostid"),
                        "monitored" to true,
                        "skipDependent" to true,
                        "filter" to mapOf("value" to 1)
                    ),
                    auth = auth()
                )
            )

            val counts = mutableMapOf<String, MutableMap<Severity, Int>>()
            resp.result.orEmpty().forEach { t ->
                val sev = Severity.fromCode(t.priority?.toIntOrNull())
                t.hosts.orEmpty().forEach { h ->
                    val id = h.hostid ?: return@forEach
                    val m = counts.getOrPut(id) { mutableMapOf() }
                    m[sev] = (m[sev] ?: 0) + 1
                }
            }
            counts
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyMap()      // si falla, igual se muestran los equipos
        }

        // 3) Mapear a modelo de interfaz
        return hosts.map { h ->
            val ifaces = h.interfaces.orEmpty()

            HostUi(
                hostId = h.hostid ?: "",
                name = h.name ?: "—",
                address = buildAddress(ifaces),
                enabled = h.status != "1",
                inMaintenance = h.maintenanceStatus == "1",
                availability = buildAvailability(ifaces),
                problems = problemsByHost[h.hostid] ?: emptyMap(),
                tags = h.tags.orEmpty().map {
                    if (it.value.isNullOrEmpty()) it.tag.orEmpty()
                    else "${it.tag}: ${it.value}"
                }
            )
        }
    }

    // IP:puerto de la interfaz principal
    private fun buildAddress(ifaces: List<ZbxInterface>): String {
        val i = ifaces.firstOrNull { it.main == "1" } ?: ifaces.firstOrNull()
            ?: return "Sin interfaz"

        val host = if (i.useip == "0" && !i.dns.isNullOrBlank()) i.dns else i.ip
        return if (i.port.isNullOrBlank()) host.orEmpty() else "${host.orEmpty()}:${i.port}"
    }

    // Una etiqueta por tipo de interfaz (ZBX / SNMP / IPMI / JMX), como en Zabbix
    private fun buildAvailability(ifaces: List<ZbxInterface>): List<AvailabilityUi> =
        ifaces
            .groupBy { it.type?.toIntOrNull() ?: 0 }
            .toSortedMap()
            .mapNotNull { (type, list) ->
                val label = when (type) {
                    1 -> "ZBX"
                    2 -> "SNMP"
                    3 -> "IPMI"
                    4 -> "JMX"
                    else -> return@mapNotNull null
                }
                val codes = list.map { it.available?.toIntOrNull() ?: 0 }
                val status = when {
                    codes.any { it == 2 } -> 2
                    codes.any { it == 1 } -> 1
                    else -> 0
                }
                AvailabilityUi(label, status)
            }
}
