package com.origin.vpn.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.origin.vpn.presentation.viewmodel.VpnViewModel
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: VpnViewModel = hiltViewModel()
) {
    val status by viewModel.vpnStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            AnimatedContent(
                targetState = status.isConnected,
                transitionSpec = {
                    fadeIn() + slideInVertically() with fadeOut() + slideOutVertically()
                }
            ) { isConnected ->
                Text(
                    text = if (isConnected) "🛡️ Protected" else "🔓 Unprotected",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isConnected) Color(0xFF00FF88) else Color(0xFFFF4444)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Main VPN Button with Glassmorphism
            GlassmorphicVpnButton(
                isConnected = status.isConnected,
                isLoading = isLoading,
                onClick = {
                    if (status.isConnected) {
                        viewModel.disconnectVpn()
                    } else {
                        viewModel.connectVpn()
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Status Card
            if (status.isConnected) {
                AnimatedVisibility(
                    visible = status.isConnected,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    VpnStatusCard(status = status)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom Navigation Placeholder
            BottomNavigationBar(navController)
        }
        
        // Error Message
        errorMessage?.let { message ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss", color = Color.White)
                    }
                }
            ) {
                Text(message, color = Color.White)
            }
        }
    }
}

@Composable
fun GlassmorphicVpnButton(
    isConnected: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .size(180.dp)
            .shadow(
                elevation = 30.dp,
                shape = CircleShape,
                clip = false,
                ambientColor = if (isConnected) Color(0xFF00FF88) else Color(0xFF00BFFF),
                spotColor = if (isConnected) Color(0xFF00FF88) else Color(0xFF00BFFF)
            )
            .scale(glow)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isConnected) Color(0xFF00FF88).copy(alpha = 0.3f) 
                            else Color(0xFF00BFFF).copy(alpha = 0.3f),
                            if (isConnected) Color(0xFF00FF88).copy(alpha = 0.1f)
                            else Color(0xFF00BFFF).copy(alpha = 0.1f)
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            if (isConnected) Color(0xFF00FF88) else Color(0xFF00BFFF),
                            if (isConnected) Color(0xFF00FF88).copy(alpha = 0.3f)
                            else Color(0xFF00BFFF).copy(alpha = 0.3f)
                        )
                    ),
                    shape = CircleShape
                ),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            enabled = !isLoading
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Text(
                        text = if (isConnected) "⏹" else "▶",
                        fontSize = 48.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isLoading) "Connecting..." 
                          else if (isConnected) "Disconnect" else "Connect",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun VpnStatusCard(status: com.origin.vpn.domain.model.VpnStatus) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E).copy(alpha = 0.8f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFF00BFFF).copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem("IP", status.ipAddress.ifEmpty { "N/A" })
                StatusItem("Ping", "${status.ping}ms")
                StatusItem("Country", status.country.ifEmpty { "N/A" })
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem("Download", formatSpeed(status.downloadSpeed))
                StatusItem("Upload", formatSpeed(status.uploadSpeed))
                StatusItem("Traffic", formatTraffic(status.trafficUsed))
            }
        }
    }
}

@Composable
fun StatusItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0F).copy(alpha = 0.9f))
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("Home", "VPN", "Device", "Settings").forEach { item ->
            TextButton(
                onClick = {
                    when (item) {
                        "Home" -> navController.navigate("home")
                        "VPN" -> navController.navigate("vpn")
                        "Device" -> navController.navigate("device")
                        "Settings" -> navController.navigate("settings")
                    }
                },
                colors = TextButtonDefaults.textButtonColors(
                    contentColor = if (item == "Home") Color(0xFF00BFFF) else Color.Gray
                )
            ) {
                Text(item, fontSize = 12.sp)
            }
        }
    }
}

// Helper functions
fun formatSpeed(bytesPerSecond: Long): String {
    return when {
        bytesPerSecond >= 1_000_000 -> "${bytesPerSecond / 1_000_000} MB/s"
        bytesPerSecond >= 1_000 -> "${bytesPerSecond / 1_000} KB/s"
        else -> "${bytesPerSecond} B/s"
    }
}

fun formatTraffic(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> "${bytes / 1_000_000_000} GB"
        bytes >= 1_000_000 -> "${bytes / 1_000_000} MB"
        bytes >= 1_000 -> "${bytes / 1_000} KB"
        else -> "$bytes B"
    }
}

package com.origin.vpn.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.origin.vpn.presentation.viewmodel.VpnViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: VpnViewModel = hiltViewModel()
) {
    val status by viewModel.vpnStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Origin VPN",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00BFFF)
                )
                
                Row {
                    IconButton(onClick = { navController.navigate("dashboard") }) {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = "Dashboard",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            }
            
            // Status Text
            AnimatedContent(
                targetState = status.isConnected,
                transitionSpec = {
                    fadeIn() + slideInVertically() with fadeOut() + slideOutVertically()
                }
            ) { isConnected ->
                Text(
                    text = if (isConnected) "🛡️ Protected" else "🔓 Unprotected",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isConnected) Color(0xFF00FF88) else Color(0xFFFF4444)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Main VPN Button
            GlassmorphicVpnButton(
                isConnected = status.isConnected,
                isLoading = isLoading,
                onClick = {
                    if (status.isConnected) {
                        viewModel.disconnectVpn()
                    } else {
                        viewModel.connectVpn()
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Status Card
            if (status.isConnected) {
                AnimatedVisibility(
                    visible = status.isConnected,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    VpnStatusCard(status = status)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom Navigation
            BottomNavigationBar(navController, currentRoute = "home")
        }
        
        // Error Message
        errorMessage?.let { message ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss", color = Color.White)
                    }
                }
            ) {
                Text(message, color = Color.White)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRoute: String = "home"
) {
    val items = listOf(
        "home" to "🏠",
        "vpn" to "🔒",
        "device" to "📱",
        "settings" to "⚙️"
    )
    
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color(0xFF0A0A0F).copy(alpha = 0.9f),
        tonalElevation = 0.dp
    ) {
        items.forEach { (route, icon) ->
            NavigationBarItem(
                icon = {
                    Text(
                        text = icon,
                        fontSize = 20.sp
                    )
                },
                label = {
                    Text(
                        text = route.capitalize(),
                        fontSize = 10.sp
                    )
                },
                selected = currentRoute == route,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF00BFFF),
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = Color(0xFF00BFFF),
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

// Keep the rest of the composables from previous phase
