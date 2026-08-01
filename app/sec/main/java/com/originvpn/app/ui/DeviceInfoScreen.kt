package com.originvpn.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.originvpn.app.ui.theme.AccentPurple
import com.originvpn.app.ui.theme.DeepSpace
import com.originvpn.app.ui.theme.NeonBlue
import com.originvpn.app.util.DeviceInfoHelper

@Composable
fun DeviceInfoScreen() {
    val context = LocalContext.current
    val specs = remember { DeviceInfoHelper.getDeviceSpecs(context) }
    var battery by remember { mutableStateOf(DeviceInfoHelper.getBatteryInfo(context)) }
    var storage by remember { mutableStateOf(DeviceInfoHelper.getStorageInfo()) }
    var ram by remember { mutableStateOf(DeviceInfoHelper.getRamInfo(context)) }

    // هر ۳ ثانیه مقادیر را دوباره از سیستم می‌خواند تا واقعی و به‌روز بماند
    LaunchedEffect(Unit) {
        while (true) {
            battery = DeviceInfoHelper.getBatteryInfo(context)
            storage = DeviceInfoHelper.getStorageInfo()
            ram = DeviceInfoHelper.getRamInfo(context)
            kotlinx.coroutines.delay(3000)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(DeepSpace)
            .padding(20.dp)
    ) {
        Text("مشخصات گوشی", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(20.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            RingStat(label = "باتری", percent = battery.percent, color = NeonBlue)
            RingStat(label = "RAM", percent = ram.usedPercent, color = AccentPurple)
            RingStat(label = "حافظه", percent = storage.usedPercent, color = Color(0xFFFFA657))
        }

        Spacer(Modifier.height(24.dp))

        InfoCard(title = "باتری") {
            InfoRow("درصد شارژ", "${battery.percent}٪")
            InfoRow("در حال شارژ", if (battery.isCharging) "بله" else "خیر")
            InfoRow("وضعیت سلامت", battery.healthText)
            InfoRow("دما", "${battery.temperatureCelsius}°C")
            InfoRow("نوع باتری", battery.technology)
        }

        Spacer(Modifier.height(12.dp))

        InfoCard(title = "حافظه") {
            InfoRow("استفاده‌شده", "%.1f GB".format(storage.usedGb))
            InfoRow("کل فضا", "%.1f GB".format(storage.totalGb))
            InfoRow("RAM استفاده‌شده", "%.1f GB".format(ram.usedGb))
            InfoRow("کل RAM", "%.1f GB".format(ram.totalGb))
        }

        Spacer(Modifier.height(12.dp))

        InfoCard(title = "مشخصات دستگاه") {
            InfoRow("مدل", specs.model)
            InfoRow("سازنده", specs.manufacturer)
            InfoRow("نسخه اندروید", specs.androidVersion)
            InfoRow("SDK", specs.sdkInt.toString())
            InfoRow("پردازنده (ABI)", specs.cpuAbi)
            InfoRow("رزولوشن صفحه", specs.screenResolution)
        }
    }
}

@Composable
private fun RingStat(label: String, percent: Int, color: Color) {
    val animatedPercent by animateFloatAsState(
        targetValue = percent / 100f,
        animationSpec = tween(800),
        label = "ring-$label"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(84.dp)) {
                val stroke = 10.dp.toPx()
                drawArc(
                    color = color.copy(alpha = 0.2f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(stroke),
                    size = Size(size.width - stroke, size.height - stroke),
                    topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2)
                )
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedPercent,
                    useCenter = false,
                    style = Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                    size = Size(size.width - stroke, size.height - stroke),
                    topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2)
                )
            }
            Text("$percent٪", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF141A2A))) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 13.sp)
    }
}
