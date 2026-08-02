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
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
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
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
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
            val builder = Builder()
                .setSession("Origin VPN")
                .setMtu(1500)
                .addAddress("10.0.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("1.1.1.1")
                .addDnsServer("8.8.8.8")
                .setBlocking(true)
                .setUnderlyingNetworks(null)
            
            // Configure VPN
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setMetered(false)
            }
            
            vpnInterface = builder.establish()
            
            if (vpnInterface != null) {
                isRunning = true
                startForeground(NOTIFICATION_ID, createNotification())
                startVpnProcessing()
                Timber.d("VPN started successfully")
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to start VPN")
            stopVpn()
        }
    }
    
    private fun startVpnProcessing() {
        serviceScope.launch {
            val inputStream = FileInputStream(vpnInterface?.fileDescriptor)
            val outputStream = FileOutputStream(vpnInterface?.fileDescriptor)
            
            // Xray Core integration will go here
            // This is where we'll process packets through Xray
            processVpnPackets(inputStream, outputStream)
        }
    }
    
    private suspend fun processVpnPackets(inputStream: FileInputStream, outputStream: FileOutputStream) {
        // This will be implemented with Xray Core in the next step
        Timber.d("VPN packet processing started")
        
        // Keep service running
        while (isRunning) {
            delay(1000)
            // Update traffic stats
        }
    }
    
    private fun stopVpn() {
        isRunning = false
        serviceScope.cancel()
        
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            Timber.e(e, "Error closing VPN interface")
        }
        
        stopForeground(true)
        Timber.d("VPN stopped")
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Origin VPN Service Channel"
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val disconnectIntent = Intent(this, VpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            1,
            disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Origin VPN")
            .setContentText("Connected - Protected")
            .setSmallIcon(R.drawable.ic_vpn)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_disconnect, "Disconnect", disconnectPendingIntent)
            .setOngoing(true)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        stopVpn()
        Timber.d("VPN Service destroyed")
    }
    
    override fun onRevoke() {
        super.onRevoke()
        stopVpn()
    }
}
