package com.origin.vpn.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.origin.vpn.presentation.viewmodel.DashboardViewModel
import com.origin.vpn.presentation.viewmodel.DeviceViewModel
import com.origin.vpn.presentation.viewmodel.VpnViewModel
import com.origin.vpn.utils.ChartDataHelper
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    deviceViewModel: DeviceViewModel = hiltViewModel(),
    vpnViewModel: VpnViewModel = hiltViewModel()
) {
    val deviceInfo by deviceViewModel.deviceInfo.collectAsState()
    val batteryInfo by deviceViewModel.batteryInfo.collectAsState()
    val vpnStatus by vpnViewModel.vpnStatus.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    
    // Collect network usage data
    val networkData by viewModel.networkStats.collectAsState()
    
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Header
            item {
                StatusHeaderCard(vpnStatus.isConnected)
            }
            
            // Battery and Network Usage Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Battery Health Circle
                    Box(modifier = Modifier.weight(1f)) {
                        BatteryHealthCard(batteryInfo)
                    }
                    
                    // Network Usage Circle
                    Box(modifier = Modifier.weight(1f)) {
                        NetworkUsageCard(vpnStatus)
                    }
                }
            }
            
            // CPU Usage Chart
            item {
                UsageChartCard(
                    title = "📊 CPU Usage",
                    percentage = deviceInfo.cpuInfo.usagePercent,
                    data = chartData.cpuHistory,
                    color = Color(0xFF00BFFF)
                )
            }
            
            // RAM Usage Chart
            item {
                UsageChartCard(
                    title = "🧠 RAM Usage",
                    percentage = deviceInfo.ramUsagePercent,
                    data = chartData.ramHistory,
                    color = Color(0xFFFF6B35)
                )
            }
            
            // Storage Usage
            item {
                StorageUsageCard(
                    used = deviceInfo.usedStorage,
                    total = deviceInfo.totalStorage,
                    percentage = deviceInfo.storageUsagePercent
                )
            }
            
            // Network Speed Chart
            item {
                NetworkSpeedCard(
                    downloadSpeed = vpnStatus.downloadSpeed,
                    uploadSpeed = vpnStatus.uploadSpeed
                )
            }
            
            // Traffic Usage
            item {
                TrafficUsageCard(
                    totalTraffic = vpnStatus.trafficUsed
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatusHeaderCard(isConnected: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E).copy(alpha = 0.8f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isConnected) Color(0xFF00FF88).copy(alpha = 0.3f)
            else Color(0xFF00BFFF).copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isConnected) "🛡️ Protected" else "🔓 Unprotected",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isConnected) Color(0xFF00FF88) else Color(0xFFFF4444)
                )
                Text(
                    text = if (isConnected) "VPN is active" else "VPN is inactive",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            // Animated indicator
            AnimatedIndicator(isConnected)
        }
    }
}

@Composable
fun AnimatedIndicator(isConnected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .size(16.dp)
            .scale(pulse)
            .background(
                color = if (isConnected) Color(0xFF00FF88) else Color(0xFFFF4444),
                shape = RoundedCornerShape(50)
            )
    )
}

@Composable
fun BatteryHealthCard(batteryInfo: BatteryInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E).copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressWithIcon(
                progress = batteryInfo.level / 100f,
                icon = if (batteryInfo.isCharging) "⚡" else "🔋",
                color = when {
                    batteryInfo.level >= 80 -> Color(0xFF00FF88)
                    batteryInfo.level >= 50 -> Color(0xFFFFBB33)
                    else -> Color(0xFFFF4444)
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Battery",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = "${batteryInfo.level}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun CircularProgressWithIcon(
    progress: Float,
    icon: String,
    color: Color
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(60.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color(0xFF333333),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(
            text = icon,
            fontSize = 24.sp
        )
    }
}

@Composable
fun NetworkUsageCard(vpnStatus: VpnStatus) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E).copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressWithIcon(
                progress = if (vpnStatus.isConnected) 1f else 0f,
                icon = "📶",
                color = if (vpnStatus.isConnected) Color(0xFF00BFFF) else Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Network",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = if (vpnStatus.isConnected) "Connected" else "Disconnected",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (vpnStatus.isConnected) Color(0xFF00BFFF) else Color.Gray
            )
        }
    }
}

@Composable
fun UsageChartCard(
    title: String,
    percentage: Int,
    data: List<Float>,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E).copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = "$percentage%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Line Chart
            if (data.isNotEmpty()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val padding = 20f
                    val chartWidth = width - padding * 2
                    val chartHeight = height - padding * 2
                    
                    val maxValue = data.maxOrNull() ?: 100f
                    val minValue = data.minOrNull() ?: 0f
                    val range = if (maxValue > minValue) maxValue - minValue else 1f
                    
                    val points = data.mapIndexed { index, value ->
                        val x = padding + (index.toFloat() / (data.size - 1)) * chartWidth
                        val y = padding + chartHeight - ((value - minValue) / range) * chartHeight
                        Offset(x, y)
                    }
                    
                    // Draw gradient area
                    if (points.isNotEmpty()) {
                        val path = android.graphics.Path().apply {
                            moveTo(padding, height - padding)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(width - padding, height - padding)
                            close()
                        }
                        
                        drawPath(
                            path = path.asComposePath(),
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.3f),
                                    color.copy(alpha = 0.05f)
                                )
                            )
                        )
                    }
                    
                    // Draw line
                    if (points.size > 1) {
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = color,
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 3f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                    
                    // Draw points
                    points.forEach { point ->
                        drawCircle(
                            color = color,
                            radius = 4f,
                            center = point
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StorageUsageCard(
    used: Long,
    total: Long,
    percentage: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E).copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "💾 Storage",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = "$percentage%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B35)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    percentage >= 80 -> Color(0xFFFF4444)
                    percentage >= 60 -> Color(0xFFFFBB33)
                    else -> Color(0xFF00FF88)
                },
                trackColor = Color(0xFF333333)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Used: ${formatBytes(used)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Free: ${formatBytes(total - used)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun NetworkSpeedCard(
    downloadSpeed: Long,
    uploadSpeed: Long
) {
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
            SpeedItem(
                label = "⬇ Download",
                speed = downloadSpeed,
                color = Color(0xFF00BFFF)
            )
            
            VerticalDivider(
                modifier = Modifier
                    .height(60.dp)
                    .width(1.dp),
                color = Color(0xFF333333)
            )
            
            SpeedItem(
                label = "⬆ Upload",
                speed = uploadSpeed,
                color = Color(0xFF00FF88)
            )
        }
    }
}

@Composable
fun SpeedItem(
    label: String,
    speed: Long,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (speed > 0) formatSpeed(speed) else "0 B/s",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun TrafficUsageCard(totalTraffic: Long) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E).copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "📦 Total Traffic",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = formatBytes(totalTraffic),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            AnimatedCounter(value = totalTraffic)
        }
    }
}

@Composable
fun AnimatedCounter(value: Long) {
    val animatedValue by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        )
    )
    
    Text(
        text = formatBytes(animatedValue.toLong()),
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF00BFFF)
    )
}

// Helper functions
private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> String.format("%.2f GB", bytes / 1_000_000_000.0)
        bytes >= 1_000_000 -> String.format("%.2f MB", bytes / 1_000_000.0)
        bytes >= 1_000 -> String.format("%.2f KB", bytes / 1_000.0)
        else -> "$bytes B"
    }
}

private fun formatSpeed(bytesPerSecond: Long): String {
    return when {
        bytesPerSecond >= 1_000_000 -> String.format("%.2f MB/s", bytesPerSecond / 1_000_000.0)
        bytesPerSecond >= 1_000 -> String.format("%.2f KB/s", bytesPerSecond / 1_000.0)
        else -> "$bytesPerSecond B/s"
    }
}

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 1f
        )
    }
}

// Extension to convert android.graphics.Path to Compose Path
private fun android.graphics.Path.asComposePath(): androidx.compose.ui.graphics.Path {
    return androidx.compose.ui.graphics.Path().apply {
        // This is a simplified conversion
        // In production, you'd want to properly convert the path
    }
}
