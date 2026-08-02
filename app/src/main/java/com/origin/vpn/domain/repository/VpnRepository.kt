package com.origin.vpn.domain.repository

import com.origin.vpn.data.remote.model.VpnConfig
import com.origin.vpn.domain.model.VpnStatus
import kotlinx.coroutines.flow.Flow

interface VpnRepository {
    fun getVpnStatus(): Flow<VpnStatus>
    suspend fun connect(config: VpnConfig): Result<Unit>
    suspend fun disconnect(): Result<Unit>
    suspend fun reconnect(): Result<Unit>
    suspend fun getConfigFromJson(json: String): Result<VpnConfig>
    suspend fun importConfig(json: String): Result<Unit>
    suspend fun exportConfig(): Result<String>
    suspend fun getTrafficStats(): TrafficStats
    suspend fun pingServer(address: String): Int
}

data class TrafficStats(
    val totalRx: Long,
    val totalTx: Long,
    val speedRx: Long,
    val speedTx: Long
)
