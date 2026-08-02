package com.originvpn.app.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs

data class BatteryInfo(
    val percent: Int,
    val isCharging: Boolean,
    val temperatureCelsius: Float,
    val healthText: String,
    val technology: String
)

data class StorageInfo(
    val usedBytes: Long,
    val totalBytes: Long
) {
    val usedGb: Double get() = usedBytes / 1_073_741_824.0
    val totalGb: Double get() = totalBytes / 1_073_741_824.0
    val usedPercent: Int get() = if (totalBytes == 0L) 0 else ((usedBytes * 100) / totalBytes).toInt()
}

data class RamInfo(
    val usedBytes: Long,
    val totalBytes: Long
) {
    val usedGb: Double get() = usedBytes / 1_073_741_824.0
    val totalGb: Double get() = totalBytes / 1_073_741_824.0
    val usedPercent: Int get() = if (totalBytes == 0L) 0 else ((usedBytes * 100) / totalBytes).toInt()
}

data class DeviceSpecs(
    val model: String,
    val manufacturer: String,
    val androidVersion: String,
    val sdkInt: Int,
    val cpuAbi: String,
    val screenResolution: String
)

object DeviceInfoHelper {

    /** خواندن واقعی وضعیت باتری از سیستم (درصد، شارژ، دما، سلامت) */
    fun getBatteryInfo(context: Context): BatteryInfo {
        val intent = context.registerReceiver(
            null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else 0

        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val temperature = tempTenths / 10f

        val healthCode = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val healthText = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "سالم"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "داغ شده"
            BatteryManager.BATTERY_HEALTH_DEAD -> "فرسوده"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "ولتاژ بالا"
            BatteryManager.BATTERY_HEALTH_COLD -> "سرد"
            else -> "نامشخص"
        }
        val technology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "-"

        return BatteryInfo(percent, isCharging, temperature, healthText, technology)
    }

    /** واقعی: فضای کل و استفاده‌شده‌ی حافظه‌ی داخلی */
    fun getStorageInfo(): StorageInfo {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.blockCountLong * stat.blockSizeLong
        val available = stat.availableBlocksLong * stat.blockSizeLong
        return StorageInfo(usedBytes = total - available, totalBytes = total)
    }

    /** واقعی: میزان RAM کل و استفاده‌شده */
    fun getRamInfo(context: Context): RamInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        val used = memInfo.totalMem - memInfo.availMem
        return RamInfo(usedBytes = used, totalBytes = memInfo.totalMem)
    }

    fun getDeviceSpecs(context: Context): DeviceSpecs {
        val dm = context.resources.displayMetrics
        return DeviceSpecs(
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            androidVersion = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "نامشخص",
            screenResolution = "${dm.widthPixels}x${dm.heightPixels}"
        )
    }
}
