package com.origin.vpn.domain.repository

import com.origin.vpn.domain.model.BatteryInfo
import com.origin.vpn.domain.model.DeviceInfo
import kotlinx.coroutines.flow.Flow

interface DeviceInfoRepository {
    fun getDeviceInfo(): Flow<DeviceInfo>
    fun getBatteryInfo(): Flow<BatteryInfo>
    suspend fun refreshDeviceInfo(): DeviceInfo
}
