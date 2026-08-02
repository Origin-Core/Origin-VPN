package com.origin.vpn.data.local.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.origin.vpn.domain.model.BatteryInfo
import com.origin.vpn.domain.model.DeviceInfo
import com.origin.vpn.domain.repository.DeviceInfoRepository
import com.origin.vpn.utils.DeviceInfoHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class DeviceInfoRepositoryImpl @Inject constructor(
    private val context: Context
) : DeviceInfoRepository {
    
    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    override fun getDeviceInfo(): Flow<DeviceInfo> = _deviceInfo.asStateFlow()
    
    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    override fun getBatteryInfo(): Flow<BatteryInfo> = _batteryInfo.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.IO)
    
    init {
        // Start periodic updates
        scope.launch {
            while (true) {
                refreshAllInfo()
                delay(2000) // Update every 2 seconds
            }
        }
    }
    
    override suspend fun refreshDeviceInfo(): DeviceInfo {
        val info = DeviceInfoHelper.getDeviceInfo(context)
        _deviceInfo.value = info
        return info
    }
    
    private suspend fun refreshAllInfo() {
        try {
            val deviceInfo = DeviceInfoHelper.getDeviceInfo(context)
            _deviceInfo.value = deviceInfo
            
            val batteryInfo = getBatteryInfoSync()
            _batteryInfo.value = batteryInfo
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing device info")
        }
    }
    
    private fun getBatteryInfoSync(): BatteryInfo {
        return try {
            val batteryStatus = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            
            if (batteryStatus != null) {
                val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                val voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                val health = getBatteryHealth(batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1))
                val isCharging = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0
                val technology = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: ""
                val capacity = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                
                BatteryInfo(
                    level = if (level >= 0 && scale > 0) (level * 100 / scale) else 0,
                    temperature = temperature,
                    voltage = voltage,
                    health = health,
                    isCharging = isCharging,
                    technology = technology,
                    capacity = capacity
                )
            } else {
                BatteryInfo()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting battery info")
            BatteryInfo()
        }
    }
    
    private fun getBatteryHealth(health: Int): String {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            else -> "Unknown"
        }
    }
}
