package com.example.monitit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.net.ssl.SSLException

data class EquiposUiState(
    val loading: Boolean = false,
    val hosts: List<HostUi> = emptyList(),
    val error: String? = null,
    val lastUpdate: String? = null
)

class EquiposViewModel : ViewModel() {

    private val repository = EquiposRepository()

    private val _state = MutableStateFlow(EquiposUiState())
    val state: StateFlow<EquiposUiState> = _state.asStateFlow()

    fun refresh() {
        if (_state.value.loading) return

        viewModelScope.launch {
            _state.update { it.copy(loading = true) }

            try {
                val list = repository.getHosts()

                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                if (ZabbixConfig.SERVER_TIME_ZONE.isNotBlank()) {
                    sdf.timeZone = TimeZone.getTimeZone(ZabbixConfig.SERVER_TIME_ZONE)
                }

                _state.value = EquiposUiState(
                    loading = false,
                    hosts = list,
                    error = null,
                    lastUpdate = sdf.format(Date())
                )

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Se conserva la última lista y se muestra el error encima
                _state.update { it.copy(loading = false, error = friendlyError(e)) }
            }
        }
    }

    private fun friendlyError(e: Exception): String = when (e) {
        is ZabbixException -> e.message ?: "Error de Zabbix"
        is UnknownHostException -> "No se encontró el servidor. Revisa la URL, la red o la VPN."
        is SocketTimeoutException, is ConnectException -> "No se pudo conectar al servidor Zabbix."
        is SSLException -> "Error de certificado SSL del servidor."
        is HttpException -> "El servidor respondió con error HTTP ${e.code()}."
        is IllegalArgumentException -> "URL de Zabbix inválida. Asegúrate de que termine en '/'."
        else -> e.message ?: "Error desconocido"
    }
}
