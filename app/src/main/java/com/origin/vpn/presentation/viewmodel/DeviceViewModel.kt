package com.origin.vpn.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.origin.vpn.domain.model.BatteryInfo
import com.origin.vpn.domain.model.DeviceInfo
import com.origin.vpn.domain.repository.DeviceInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val repository: DeviceInfoRepository
) : ViewModel() {
    
    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    val deviceInfo: StateFlow<DeviceInfo> = _deviceInfo.asStateFlow()
    
    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.getDeviceInfo().collect { info ->
                _deviceInfo.value = info
            }
        }
        
        viewModelScope.launch {
            repository.getBatteryInfo().collect { info ->
                _batteryInfo.value = info
            }
        }
    }
    
    fun refreshDeviceInfo() {
        viewModelScope.launch {
            repository.refreshDeviceInfo()
        }
    }
}
