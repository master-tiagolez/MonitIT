package com.example.monitit

/* =========================================================
   DASHBOARD: equipos funcionando vs. críticos (solo por ICMP)
   ========================================================= */

data class DashboardStats(
    val total: Int,        // equipos monitoreados (habilitados)
    val working: Int,      // responden al ping
    val critical: Int      // sin respuesta ICMP
)

class DashboardRepository {

    companion object {
        // Nombre del trigger de Zabbix que indica que el equipo no responde al ping.
        // Es el mismo texto que aparece en Alertas: "Unavailable by ICMP ping".
        private const val ICMP_TRIGGER = "Unavailable by ICMP ping"
        private const val MAX_HOSTS = 1000
    }

    private val api: ZabbixApi
        get() = ZabbixClient.api

    private fun auth(): String? =
        if (ZabbixConfig.USE_BEARER_HEADER) null else ZabbixConfig.API_TOKEN

    suspend fun getStats(): DashboardStats {

        // 1) Equipos monitoreados (habilitados)
        val hostsResp = api.hostGet(
            RpcRequest(
                method = "host.get",
                params = mapOf(
                    "output" to listOf("hostid"),
                    "monitored_hosts" to true,
                    "limit" to MAX_HOSTS
                ),
                auth = auth()
            )
        )
        hostsResp.error?.let { throw ZabbixException(it) }

        val monitored: Set<String> =
            hostsResp.result.orEmpty().mapNotNull { it.hostid }.toSet()

        // 2) Equipos con el problema "Unavailable by ICMP ping" activo.
        //    Si esta consulta falla se lanza el error (no se muestra un "todo OK" falso).
        val trigResp = api.triggerGet(
            RpcRequest(
                method = "trigger.get",
                id = 2,
                params = mapOf(
                    "output" to listOf("triggerid"),
                    "selectHosts" to listOf("hostid"),
                    "search" to mapOf("description" to ICMP_TRIGGER),
                    "monitored" to true,
                    "skipDependent" to true,
                    "filter" to mapOf("value" to 1)
                ),
                auth = auth()
            )
        )
        trigResp.error?.let { throw ZabbixException(it) }

        val down: Set<String> = trigResp.result.orEmpty()
            .flatMap { it.hosts.orEmpty() }
            .mapNotNull { it.hostid }
            .filter { it in monitored }
            .toSet()

        return DashboardStats(
            total = monitored.size,
            working = monitored.size - down.size,
            critical = down.size
        )
    }
}
