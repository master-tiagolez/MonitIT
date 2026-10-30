package com.example.monitit

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

/* =========================================================
   CONFIGURACIÓN  ZABBIX
   ========================================================= */

object ZabbixConfig {

    //
    //  "http://IP/zabbix/"
    // const val BASE_URL = "http://192.168.0.109/zabbix/"
    const val BASE_URL = "http://192.168.137.82//zabbix/"
    // Token creado en Zabbix: API
    //const val API_TOKEN = "7d81567867a2cdf239605eab7181c6b50f00c56fcbff6cec12a323a5e998f60b"
    const val API_TOKEN = "a41f448be712b0392bc8f5c347c1897636d94e4c78d02d60637027456be0c8f8"
    const val USE_BEARER_HEADER = true

    // Cada cuánto se refresca la pantalla de Alertas (ms)
    const val REFRESH_MS = 60_000L

    // Máximo de problemas que se traen por consulta
    const val MAX_PROBLEMS = 100

    // Zona horaria para mostrar las horas (Bogotá / Colombia = "America/Bogota" o "GMT-5")
    const val SERVER_TIME_ZONE = "America/Bogota"
}



data class RpcRequest(
    val jsonrpc: String = "2.0",
    val method: String,
    val params: Map<String, Any>,
    val id: Int = 1,
    val auth: String? = null          // null = no se envía
)

data class RpcError(
    val code: Int? = null,
    val message: String? = null,
    val data: String? = null
)

data class RpcResponse<T>(
    val result: T? = null,
    val error: RpcError? = null
)

data class ZbxTag(
    val tag: String? = null,
    val value: String? = null
)

data class ZbxProblem(
    val eventid: String? = null,
    val objectid: String? = null,
    val name: String? = null,
    val severity: String? = null,
    val clock: String? = null,
    @SerializedName("r_eventid") val rEventId: String? = null,
    @SerializedName("r_clock") val rClock: String? = null,
    val acknowledged: String? = null,
    val tags: List<ZbxTag>? = null
)

data class ZbxHost(
    val hostid: String? = null,
    val name: String? = null
)

data class ZbxEvent(
    val eventid: String? = null,
    val hosts: List<ZbxHost>? = null
)

class ZabbixException(error: RpcError) :
    Exception("${error.message ?: "Error de Zabbix"}${error.data?.let { ": $it" } ?: ""}")


/* =========================================================
   MODELO PARA LA INTERFAZ
   ========================================================= */

// Colores por defecto de Zabbix
enum class Severity(val label: String, val color: Long, val darkText: Boolean) {
    NO_CLASIFICADA("No clasificada", 0xFF97AAB3, false),
    INFORMATIVA("Informativa", 0xFF7499FF, false),
    ADVERTENCIA("Advertencia", 0xFFFFC859, true),
    PROMEDIO("Promedio", 0xFFFFA059, true),
    ALTA("Alta", 0xFFE97659, false),
    CRITICA("Crítica", 0xFFE45959, false);

    companion object {
        fun fromCode(code: Int?): Severity =
            values().getOrElse(code ?: 0) { NO_CLASIFICADA }
    }
}

data class ProblemUi(
    val eventId: String,
    val time: String,
    val severity: Severity,
    val host: String,
    val problem: String,
    val resolved: Boolean,
    val acknowledged: Boolean,
    val duration: String,
    val tags: List<String>
)


/* =========================================================
   API RETROFIT
   ========================================================= */

interface ZabbixApi {

    @POST("api_jsonrpc.php")
    suspend fun problemGet(@Body body: RpcRequest): RpcResponse<List<ZbxProblem>>

    @POST("api_jsonrpc.php")
    suspend fun eventGet(@Body body: RpcRequest): RpcResponse<List<ZbxEvent>>

    // NUEVAS
    @POST("api_jsonrpc.php")
    suspend fun hostGet(@Body body: RpcRequest): RpcResponse<List<ZbxHostFull>>

    @POST("api_jsonrpc.php")
    suspend fun triggerGet(@Body body: RpcRequest): RpcResponse<List<ZbxTrigger>>
}

object ZabbixClient {

    val api: ZabbixApi by lazy {

        val http = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                if (ZabbixConfig.USE_BEARER_HEADER) {
                    builder.header("Authorization", "Bearer ${ZabbixConfig.API_TOKEN}")
                }
                chain.proceed(builder.build())
            }
            .build()

        Retrofit.Builder()
            .baseUrl(ZabbixConfig.BASE_URL)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ZabbixApi::class.java)
    }
}


/* =========================================================
   REPOSITORIO
   Replica "Problemas  Problemas recientes" de Zabbix.
   ========================================================= */

class ZabbixRepository {

    private val api: ZabbixApi
        get() = ZabbixClient.api

    private fun auth(): String? =
        if (ZabbixConfig.USE_BEARER_HEADER) null else ZabbixConfig.API_TOKEN

    suspend fun getRecentProblems(): List<ProblemUi> {

        // 1) Problemas recientes (activos + resueltos hace poco), con etiquetas
        val problemsResp = api.problemGet(
            RpcRequest(
                method = "problem.get",
                params = mapOf(
                    "output" to "extend",
                    "recent" to true,
                    "selectTags" to "extend",
                    "sortfield" to listOf("eventid"),
                    "sortorder" to "DESC",
                    "limit" to ZabbixConfig.MAX_PROBLEMS
                ),
                auth = auth()
            )
        )

        problemsResp.error?.let { throw ZabbixException(it) }

        val problems = problemsResp.result.orEmpty()
        if (problems.isEmpty()) return emptyList()

        // 2) Nombre del equipo (problem.get no lo trae)
        val hostByEvent: Map<String, String> = try {
            val eventsResp = api.eventGet(
                RpcRequest(
                    method = "event.get",
                    id = 2,
                    params = mapOf(
                        "output" to listOf("eventid"),
                        "eventids" to problems.mapNotNull { it.eventid },
                        "selectHosts" to listOf("hostid", "name")
                    ),
                    auth = auth()
                )
            )
            eventsResp.result.orEmpty().associate {
                (it.eventid ?: "") to (it.hosts?.firstOrNull()?.name ?: "—")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyMap()      // si falla, igual se muestran los problemas
        }

        // 3) Mapear a modelo de interfaz
        val nowSec = System.currentTimeMillis() / 1000

        return problems.map { p ->
            val start = p.clock?.toLongOrNull() ?: 0L
            val resolved = !p.rEventId.isNullOrEmpty() && p.rEventId != "0"
            val end = if (resolved) (p.rClock?.toLongOrNull() ?: nowSec) else nowSec

            ProblemUi(
                eventId = p.eventid ?: "",
                time = formatTime(start, nowSec),
                severity = Severity.fromCode(p.severity?.toIntOrNull()),
                host = hostByEvent[p.eventid] ?: "—",
                problem = p.name ?: "",
                resolved = resolved,
                acknowledged = p.acknowledged == "1",
                duration = formatDuration(end - start),
                tags = p.tags.orEmpty().map {
                    if (it.value.isNullOrEmpty()) it.tag.orEmpty()
                    else "${it.tag}: ${it.value}"
                }
            )
        }
    }

    private fun formatTime(startSec: Long, nowSec: Long): String {
        val pattern = if (nowSec - startSec > 86_400) "dd/MM HH:mm:ss" else "HH:mm:ss"
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        if (ZabbixConfig.SERVER_TIME_ZONE.isNotBlank()) {
            sdf.timeZone = TimeZone.getTimeZone(ZabbixConfig.SERVER_TIME_ZONE)
        }
        return sdf.format(Date(startSec * 1000))
    }

    // Intervalos de monitoreo
    private fun formatDuration(totalSec: Long): String {
        var s = maxOf(totalSec, 0L)
        val d = s / 86_400; s %= 86_400
        val h = s / 3_600;  s %= 3_600
        val m = s / 60;     s %= 60
        return when {
            d > 0 -> "${d}d ${h}h ${m}m"
            h > 0 -> "${h}h ${m}m ${s}s"
            m > 0 -> "${m}m ${s}s"
            else -> "${s}s"
        }
    }
}
