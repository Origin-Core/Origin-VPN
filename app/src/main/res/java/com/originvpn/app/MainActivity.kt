package com.originvpn.app

import android.Manifest
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.originvpn.app.ui.DeviceInfoScreen
import com.originvpn.app.ui.VpnScreen
import com.originvpn.app.ui.theme.OriginVpnTheme
import com.originvpn.app.vpn.OriginVpnService

class MainActivity : ComponentActivity() {

    // اجازه‌ی رسمی VPN از سیستم؛ بدون این دیالوگ سیستمی هیچ VpnService‌ای اجازه‌ی
    // establish() گرفتن ندارد — این همان دیالوگ استاندارد "درخواست اتصال VPN" اندروید است.
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startVpnService()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* نتیجه لازم نیست هندل شود، صرفا برای نمایش نوتیف اتصال است */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            OriginVpnTheme {
                var selectedTab by remember { mutableStateOf(0) }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Filled.Shield, contentDescription = null) },
                                label = { Text("VPN") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Filled.PhoneAndroid, contentDescription = null) },
                                label = { Text("مشخصات گوشی") }
                            )
                        }
                    }
                ) { padding ->
                    Box(Modifier.padding(padding)) {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                fadeIn(tween(250)) togetherWith fadeOut(tween(200))
                            },
                            label = "tab"
                        ) { tab ->
                            when (tab) {
                                0 -> VpnScreen(onConnectRequested = { requestVpnPermissionThenConnect() })
                                1 -> DeviceInfoScreen()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestVpnPermissionThenConnect() {
        val prepareIntent = VpnService.prepare(this)
        if (prepareIntent != null) {
            // اولین بار: سیستم دیالوگ اعتماد VPN را نشان می‌دهد
            vpnPermissionLauncher.launch(prepareIntent)
        } else {
            // قبلا اجازه داده شده
            startVpnService()
        }
    }

    private fun startVpnService() {
        val intent = Intent(this, OriginVpnService::class.java).apply {
            action = OriginVpnService.ACTION_CONNECT
            putExtra(OriginVpnService.EXTRA_CONFIG_JSON, com.originvpn.app.data.DefaultServer.CONFIG_JSON)
        }
        startForegroundService(intent)
    }
}
