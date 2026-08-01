package com.originvpn.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import com.originvpn.app.data.DefaultServer
import com.originvpn.app.ui.theme.AccentPurple
import com.originvpn.app.ui.theme.DeepSpace
import com.originvpn.app.ui.theme.NeonBlue
import com.originvpn.app.vpn.OriginVpnService
import com.originvpn.app.vpn.VlessConfig

enum class ConnState { DISCONNECTED, CONNECTING, CONNECTED }

@Composable
fun VpnScreen(onConnectRequested: () -> Unit) {
    var state by remember { mutableStateOf(ConnState.DISCONNECTED) }
    val server = remember { VlessConfig.fromXrayJson(DefaultServer.CONFIG_JSON) }
    val context = LocalContext.current

    // انیمیشن ضربان (pulse) دور دکمه‌ی اتصال وقتی در حال وصل شدن یا وصل است
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val ringColor by animateColorAsState(
        targetValue = when (state) {
            ConnState.CONNECTED -> NeonBlue
            ConnState.CONNECTING -> AccentPurple
            ConnState.DISCONNECTED -> Color.Gray
        },
        animationSpec = tween(500),
        label = "ringColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepSpace, Color(0xFF11162A))))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Origin VPN", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(server.remarks, fontSize = 14.sp, color = Color.Gray)

        Spacer(Modifier.weight(1f))

        Box(contentAlignment = Alignment.Center) {
            // حلقه‌ی بیرونی متحرک
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .scale(if (state != ConnState.DISCONNECTED) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(ringColor.copy(alpha = 0.15f))
            )
            // دکمه‌ی اصلی اتصال
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(ringColor, ringColor.copy(alpha = 0.6f))))
                    .clickable {
                        when (state) {
                            ConnState.DISCONNECTED -> {
                                state = ConnState.CONNECTING
                                onConnectRequested()
                                // پس از establish شدن واقعی TUN، سرویس باید این وضعیت را
                                // (مثلا با Broadcast یا StateFlow) به UI اعلام کند؛
                                // اینجا برای سادگی مستقیم CONNECTED در نظر گرفته می‌شود.
                                state = ConnState.CONNECTED
                            }
                            else -> {
                                context.startService(
                                    Intent(context, OriginVpnService::class.java).apply {
                                        action = OriginVpnService.ACTION_DISCONNECT
                                    }
                                )
                                state = ConnState.DISCONNECTED
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Power,
                    contentDescription = "Connect",
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = when (state) {
                ConnState.DISCONNECTED -> "قطع — برای اتصال ضربه بزنید"
                ConnState.CONNECTING -> "در حال برقراری اتصال..."
                ConnState.CONNECTED -> "متصل"
            },
            fontSize = 16.sp,
            color = Color.White
        )

        Spacer(Modifier.weight(1f))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141A2A))
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("سرور", color = Color.Gray)
                    Text(server.remarks, color = Color.White)
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("پروتکل", color = Color.Gray)
                    Text("VLESS / ${server.network}", color = Color.White)
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("آدرس", color = Color.Gray)
                    Text("${server.address}:${server.port}", color = Color.White)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
