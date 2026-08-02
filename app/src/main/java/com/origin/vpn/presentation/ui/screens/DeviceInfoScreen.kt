package com.origin.vpn.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.origin.vpn.presentation.viewmodel.DeviceViewModel
import com.origin.vpn.utils.formatBytes
import kotlinx.coroutines.delay

@Composable
fun DeviceInfoScreen(
    viewModel: DeviceViewModel = hiltViewModel()
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0F),
                        Color(0xFF1A1A3E),
                        Color(0xFF0A0A0F)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Battery Section
            item {
                BatteryCard(batteryInfo)
            }
            
            // Device Info Section
            item {
                InfoSectionCard(
                    title = "📱 Device Information",
                    items = listOf(
                        "Model" to deviceInfo.model,
                        "Brand" to deviceInfo.brand,
                        "Manufacturer" to deviceInfo.manufacturer,
                        "Android Version" to deviceInfo.androidVersion,
                        "SDK Version" to deviceInfo.sdkVersion.toString(),
                        "Device Name" to deviceInfo.deviceName
                    )
                )
            }
            
            // CPU & GPU Section
            item {
                InfoSectionCard(
                    title = "⚡ CPU & GPU",
                    items = listOf(
                        "CPU Cores" to deviceInfo.cpuInfo.cores.toString(),
                        "Max Frequency" to "${deviceInfo.cpuInfo.maxFreq} MHz",
                        "Min Frequency" to "${deviceInfo.cpuInfo.minFreq} MHz",
                        "Current Frequency" to "${deviceInfo.cpuInfo.currentFreq} MHz",
                        "CPU Usage" to "${deviceInfo.cpuInfo.usagePercent}%",
                        "GPU" to deviceInfo.gpuInfo
                    )
                )
            }
            
            // Memory Section
            item {
                InfoSectionCard(
                    title = "🧠 Memory",
                    items = listOf(
                        "Total RAM" to formatBytes(deviceInfo.totalRam),
                        "Used RAM" to formatBytes(deviceInfo.usedRam),
                        "Free RAM" to formatBytes(deviceInfo.freeRam),
                        "RAM Usage" to "${deviceInfo.ramUsagePercent}%"
                    )
                )
            }
            
            // Storage Section
            item {
                InfoSectionCard(
                    title = "💾 Storage",
                    items = listOf(
                        "Total Storage" to formatBytes(deviceInfo.totalStorage),
                        "Used Storage" to formatBytes(deviceInfo.usedStorage),
                        "Free Storage" to formatBytes(deviceInfo.freeStorage),
                        "Storage Usage" to "${deviceInfo.storageUsagePercent}%"
                    )
                )
            }
            
            // Network Section
            item {
                InfoSectionCard(
                    title = "🌐 Network",
                    items = listOf(
                        "Network Type" to deviceInfo.networkType,
                        "WiFi SSID" to deviceInfo.wifiInfo.ssid,
                        "WiFi Signal" to "${deviceInfo.wifiInfo.strength}%",
                        "WiFi Frequency" to "${deviceInfo.wifiInfo.frequency} MHz",
                        "IP Address" to deviceInfo.ipAddress,
                        "MAC Address" to deviceInfo.macAddress
                    )
                )
            }
            
            // System Section
            item {
                InfoSectionCard(
                    title = "🔒 System",
                    items = listOf(
                        "Uptime" to formatUptime(deviceInfo.uptime),
                        "Security Enabled" to if (deviceInfo.isSecurityEnabled) "✅ Yes" else "❌ No",
                        "Rooted" to if (deviceInfo.isRooted) "⚠️ Yes" else "✅ No"
                    )
                )
            }
        }
    }
}

@Composable
fun BatteryCard(batteryInfo: BatteryInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E).copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Battery Level Circle
            BatteryLevelCircle(level = batteryInfo.level)
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${batteryInfo.level}%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = batteryInfo.health,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = if (batteryInfo.isCharging) "⚡ Charging" else "🔋 Not Charging",
                    fontSize = 12.sp,
                    color = if (batteryInfo.isCharging) Color(0xFF00FF88) else Color.Gray
                )
            }
            
            Column {
                Text(
                    text = "${batteryInfo.temperature}°C",
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "${batteryInfo.voltage} mV",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = batteryInfo.technology,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun BatteryLevelCircle(level: Int) {
    val color = when {
        level >= 80 -> Color(0xFF00FF88)
        level >= 50 -> Color(0xFFFFBB33)
        else -> Color(0xFFFF4444)
    }
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(
                color = Color.Transparent
            )
    ) {
        // Circular progress indicator
        androidx.compose.material3.CircularProgressIndicator(
            progress = level / 100f,
            modifier = Modifier.fillMaxSize(),
            color = color,
            strokeWidth = 8.dp,
            trackColor = Color(0xFF333333)
        )
        Text(
            text = "$level%",
            modifier = Modifier.align(Alignment.Center),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun InfoSectionCard(
    title: String,
    items: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E).copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00BFFF),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = value.ifEmpty { "N/A" },
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun formatUptime(seconds: Long): String {
    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        days > 0 -> "${days}d ${hours}h ${minutes}m"
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}

fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> "${bytes / 1_000_000_000} GB"
        bytes >= 1_000_000 -> "${bytes / 1_000_000} MB"
        bytes >= 1_000 -> "${bytes / 1_000} KB"
        else -> "$bytes B"
    }
}
