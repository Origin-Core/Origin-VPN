package com.origin.vpn.data.local.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.origin.vpn.R
import com.origin.vpn.presentation.MainActivity
import timber.log.Timber
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.*

class VpnService : VpnService() {
    
    companion object {
        const val ACTION_CONNECT = "com.origin.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.origin.vpn.DISCONNECT"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "vpn_channel"
        
        private var instance: VpnService? = null
        fun getInstance(): VpnService? = instance
    }
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private var serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false
    private var config: String = ""
    
    // Xray Core integration
    private lateinit var xrayCore: XrayCore
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        
        // Initialize Xray Core
        try {
            xrayCore = XrayCore(this)
            xrayCore.initialize()
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Xray Core")
        }
        
        Timber.d("VPN Service created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                ACTION_CONNECT -> {
                    val configJson = intent.getStringExtra("config") ?: return START_STICKY
                    config = configJson
                    startVpn()
                }
                ACTION_DISCONNECT -> {
                    stopVpn()
                }
            }
        }
        return START_STICKY
    }
    
    private fun startVpn() {
        if (isRunning) {
            Timber.d("VPN already running")
            return
        }
        
        try {
            // Build VPN interface
            val builder
