package com.origin.vpn.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.origin.vpn.domain.model.VpnStatus
import com.origin.vpn.domain.repository.VpnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

data class ChartData(
    val cpuHistory: List<Float> = listOf(0f),
    val ramHistory: List<Float> = listOf(0f),
    val networkHistory: List<Float> = listOf(0f)
)

data class NetworkStats(
    val downloadSpeed: Long = 0,
    val uploadSpeed: Long = 0,
    val totalRx: Long = 0,
    val totalTx: Long = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val vpnRepository: VpnRepository
) : ViewModel() {
    
    private val _chartData = MutableStateFlow(ChartData())
    val chartData: StateFlow<ChartData> = _chartData.asStateFlow()
    
    private val _networkStats = MutableStateFlow(NetworkStats())
    val networkStats: StateFlow<NetworkStats> = _networkStats.asStateFlow()
    
    private val cpuHistory = mutableListOf<Float>()
    private val ramHistory = mutableListOf<Float>()
    private val networkHistory = mutableListOf<Float>()
    
    init {
        // Initialize with default data
        repeat(20) {
            cpuHistory.add((10 + Math.random() * 40).toFloat())
            ramHistory.add((30 + Math.random() * 40).toFloat())
            networkHistory.add((5 + Math.random() * 20).toFloat())
        }
        
        viewModelScope.launch {
            while (true) {
                updateCharts()
                delay(2000) // Update every 2 seconds
            }
        }
        
        viewModelScope.launch {
            vpnRepository.getVpnStatus().collect { status ->
                updateNetworkStats(status)
            }
        }
    }
    
    private fun updateCharts() {
        // Update CPU history
        cpuHistory.removeFirstOrNull()
        cpuHistory.add((10 + Math.random() * 60).toFloat())
        
        // Update RAM history
        ramHistory.removeFirstOrNull()
        ramHistory.add((30 + Math.random() * 50).toFloat())
        
        // Update network history
        networkHistory.removeFirstOrNull()
        networkHistory.add((5 + Math.random() * 30).toFloat())
        
        _chartData.value = ChartData(
            cpuHistory = cpuHistory.toList(),
            ramHistory = ramHistory.toList(),
            networkHistory = networkHistory.toList()
        )
    }
    
    private fun updateNetworkStats(status: VpnStatus) {
        _networkStats.value = NetworkStats(
            downloadSpeed = status.downloadSpeed,
            uploadSpeed = status.uploadSpeed,
            totalRx = status.trafficUsed,
            totalTx = status.trafficUsed / 2 // Simulated
        )
    }
}
