package com.origin.vpn.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.origin.vpn.data.remote.model.VpnConfig
import com.origin.vpn.domain.model.VpnStatus
import com.origin.vpn.domain.repository.VpnRepository
import com.origin.vpn.domain.usecase.ConnectVpnUseCase
import com.origin.vpn.domain.usecase.DisconnectVpnUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class VpnViewModel @Inject constructor(
    private val connectUseCase: ConnectVpnUseCase,
    private val disconnectUseCase: DisconnectVpnUseCase,
    private val repository: VpnRepository
) : ViewModel() {
    
    private val _vpnStatus = MutableStateFlow(VpnStatus())
    val vpnStatus: StateFlow<VpnStatus> = _vpnStatus.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Sample config (will be replaced with user's config)
    private val sampleConfig = VpnConfig(
        address = "your-server.com",
        port = 443,
        id = "your-uuid-here",
        network = "ws",
        security = "auto",
        path = "/your-path",
        host = "your-host.com"
    )
    
    init {
        // Start listening to VPN status
        viewModelScope.launch {
            repository.getVpnStatus().collect { status ->
                _vpnStatus.value = status
            }
        }
    }
    
    fun connectVpn() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // In production, load config from user input or saved config
                val result = connectUseCase(sampleConfig)
                if (result.isSuccess) {
                    Timber.d("VPN connected successfully")
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Connection failed"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Connection error"
                Timber.e(e, "VPN connection error")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun disconnectVpn() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = disconnectUseCase()
                if (result.isSuccess) {
                    Timber.d("VPN disconnected successfully")
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Disconnect failed"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Disconnect error"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
