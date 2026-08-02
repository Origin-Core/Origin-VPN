package com.origin.vpn.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeviceInfo(
    // Device Info
    val model: String = "",
    val brand: String = "",
    val manufacturer: String = "",
    val androidVersion: String = "",
    val sdkVersion: Int = 0,
    val deviceName: String = "",
    
    // CPU & GPU
    val cpuInfo: CpuInfo = CpuInfo(),
    val gpuInfo: String = "",
    
    // Memory
    val totalRam: Long = 0,
    val usedRam: Long = 0,
    val freeRam: Long = 0,
    val ramUsagePercent: Int = 0,
    
    // Storage
    val totalStorage: Long = 0,
    val usedStorage: Long = 0,
    val freeStorage: Long = 0,
    val storageUsagePercent: Int = 0,
    
    // Network
    val networkType: String = "",
    val wifiInfo: WifiInfo = WifiInfo(),
    val ipAddress: String = "",
    val macAddress: String = "",
    
    // System
    val uptime: Long = 0,
    val isSecurityEnabled: Boolean = false,
    val isRooted: Boolean = false
) : Parcelable

@Parcelize
data class CpuInfo(
    val cores: Int = 0,
    val maxFreq: Long = 0,
    val minFreq: Long = 0,
    val currentFreq: Long = 0,
    val usagePercent: Int = 0
) : Parcelable

@Parcelize
data class WifiInfo(
    val ssid: String = "",
    val strength: Int = 0,
    val frequency: Int = 0
) : Parcelable

@Parcelize
data class BatteryInfo(
    val level: Int = 0,
    val temperature: Float = 0f,
    val voltage: Int = 0,
    val health: String = "",
    val isCharging: Boolean = false,
    val technology: String = "",
    val capacity: Int = 0
) : Parcelable
