package com.origin.vpn.utils

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings
import android.telephony.TelephonyManager
import com.origin.vpn.domain.model.CpuInfo
import com.origin.vpn.domain.model.DeviceInfo
import com.origin.vpn.domain.model.WifiInfo
import timber.log.Timber
import java.io.File
import java.io.RandomAccessFile

object DeviceInfoHelper {
    
    fun getDeviceInfo(context: Context): DeviceInfo {
        return DeviceInfo(
            model = Build.MODEL,
            brand = Build.BRAND,
            manufacturer = Build.MANUFACTURER,
            androidVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            deviceName = Build.DEVICE,
            cpuInfo = getCpuInfo(),
            gpuInfo = getGpuInfo(),
            totalRam = getTotalRam(),
            usedRam = getUsedRam(context),
            freeRam = getFreeRam(context),
            ramUsagePercent = getRamUsagePercent(context),
            totalStorage = getTotalStorage(),
            usedStorage = getUsedStorage(),
            freeStorage = getFreeStorage(),
            storageUsagePercent = getStorageUsagePercent(),
            networkType = getNetworkType(context),
            wifiInfo = getWifiInfo(context),
            ipAddress = getIpAddress(context),
            macAddress = getMacAddress(context),
            uptime = SystemClock.elapsedRealtime() / 1000,
            isSecurityEnabled = isSecurityEnabled(context),
            isRooted = isDeviceRooted()
        )
    }
    
    private fun getCpuInfo(): CpuInfo {
        return try {
            val cores = Runtime.getRuntime().availableProcessors()
            val maxFreq = readCpuFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
            val minFreq = readCpuFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq")
            val currentFreq = readCpuFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            val usage = getCpuUsage()
            
            CpuInfo(
                cores = cores,
                maxFreq = maxFreq,
                minFreq = minFreq,
                currentFreq = currentFreq,
                usagePercent = usage
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting CPU info")
            CpuInfo()
        }
    }
    
    private fun readCpuFile(path: String): Long {
        return try {
            val file = File(path)
            if (file.exists()) {
                file.readText().trim().toLong() / 1000 // Convert to MHz
            } else {
                0
            }
        } catch (e: Exception) {
            Timber.e(e, "Error reading CPU file: $path")
            0
        }
    }
    
    private fun getCpuUsage(): Int {
        return try {
            val stat1 = readStatFile()
            Thread.sleep(500)
            val stat2 = readStatFile()
            
            val idle1 = stat1["idle"] ?: 0
            val total1 = stat1["total"] ?: 0
            val idle2 = stat2["idle"] ?: 0
            val total2 = stat2["total"] ?: 0
            
            if (total2 > total1) {
                val diffIdle = idle2 - idle1
                val diffTotal = total2 - total1
                100 - (diffIdle * 100 / diffTotal)
            } else {
                0
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting CPU usage")
            0
        }
    }
    
    private fun readStatFile(): Map<String, Long> {
        val values = mutableMapOf<String, Long>()
        try {
            val file = File("/proc/stat")
            val parts = file.readText().split("\\s+".toRegex())
            if (parts.isNotEmpty() && parts[0] == "cpu") {
                val user = parts[1].toLongOrNull() ?: 0
                val nice = parts[2].toLongOrNull() ?: 0
                val system = parts[3].toLongOrNull() ?: 0
                val idle = parts[4].toLongOrNull() ?: 0
                val total = user + nice + system + idle
                
                values["idle"] = idle
                values["total"] = total
            }
        } catch (e: Exception) {
            Timber.e(e, "Error reading stat file")
        }
        return values
    }
    
    private fun getGpuInfo(): String {
        return try {
            val file = File("/sys/class/kgsl/kgsl-3d0/gpu_model")
            if (file.exists()) {
                file.readText().trim()
            } else {
                val file2 = File("/sys/class/graphics/fb0/device/vendor")
                if (file2.exists()) {
                    file2.readText().trim()
                } else {
                    "Unknown"
                }
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getTotalRam(): Long {
        val memInfo = ActivityManager.MemoryInfo()
        val activityManager = AppContextProvider.get().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(memInfo)
        return memInfo.totalMem
    }
    
    private fun getUsedRam(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return memInfo.totalMem - memInfo.availMem
    }
    
    private fun getFreeRam(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return memInfo.availMem
    }
    
    private fun getRamUsagePercent(context: Context): Int {
        val total = getTotalRam()
        val used = getUsedRam(context)
        return if (total > 0) ((used.toFloat() / total) * 100).toInt() else 0
    }
    
    private fun getTotalStorage(): Long {
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return blockSize * totalBlocks
    }
    
    private fun getUsedStorage(): Long {
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        val totalBlocks = stat.blockCountLong
        return blockSize * (totalBlocks - availableBlocks)
    }
    
    private fun getFreeStorage(): Long {
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return blockSize * availableBlocks
    }
    
    private fun getStorageUsagePercent(): Int {
        val total = getTotalStorage()
        val used = getUsedStorage()
        return if (total > 0) ((used.toFloat() / total) * 100).toInt() else 0
    }
    
    private fun getNetworkType(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "None"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "None"
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Unknown"
        }
    }
    
    private fun getWifiInfo(context: Context): WifiInfo {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            WifiInfo(
                ssid = wifiInfo.ssid?.replace("\"", "") ?: "",
                strength = wifiManager.calculateSignalLevel(wifiInfo.rssi, 100),
                frequency = wifiInfo.frequency
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting WiFi info")
            WifiInfo()
        }
    }
    
    private fun getIpAddress(context: Context): String {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ip = wifiInfo.ipAddress
            "${ip and 0xFF}.${(ip shr 8) and 0xFF}.${(ip shr 16) and 0xFF}.${(ip shr 24) and 0xFF}"
        } catch (e: Exception) {
            "N/A"
        }
    }
    
    private fun getMacAddress(context: Context): String {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            wifiInfo.macAddress ?: "N/A"
        } catch (e: Exception) {
            "N/A"
        }
    }
    
    private fun isSecurityEnabled(context: Context): Boolean {
        // Check if device has secure lock screen
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
        return keyguardManager.isDeviceSecure
    }
    
    private fun isDeviceRooted(): Boolean {
        return try {
            val suFile = File("/system/bin/su")
            val suFile2 = File("/system/xbin/su")
            val suFile3 = File("/system/app/SuperSU.apk")
            suFile.exists() || suFile2.exists() || suFile3.exists()
        } catch (e: Exception) {
            false
        }
    }
}

// AppContextProvider for accessing context globally
object AppContextProvider {
    private var context: Context? = null
    
    fun init(context: Context) {
        this.context = context.applicationContext
    }
    
    fun get(): Context {
        return context ?: throw IllegalStateException("AppContextProvider not initialized")
    }
}
