package com.origin.vpn.data.local.service

import android.content.Context
import timber.log.Timber
import java.io.File

class XrayCore(private val context: Context) {
    
    companion object {
        init {
            // Load native libraries
            System.loadLibrary("v2ray")
            System.loadLibrary("xray")
        }
    }
    
    private var isRunning = false
    
    // Native methods
    private external fun nativeInit(): Boolean
    private external fun nativeStart(config: String): Boolean
    private external fun nativeStop(): Boolean
    private external fun nativeProcessPacket(data: ByteArray, length: Int): ByteArray
    
    fun initialize(): Boolean {
        return try {
            val result = nativeInit()
            Timber.d("Xray Core initialized: $result")
            result
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Xray Core")
            false
        }
    }
    
    fun start(config: String): Boolean {
        return try {
            val result = nativeStart(config)
            if (result) {
                isRunning = true
                Timber.d("Xray Core started")
            } else {
                Timber.e("Failed to start Xray Core")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "Error starting Xray Core")
            false
        }
    }
    
    fun stop(): Boolean {
        return try {
            if (isRunning) {
                val result = nativeStop()
                if (result) {
                    isRunning = false
                    Timber.d("Xray Core stopped")
                }
                result
            } else {
                true
            }
        } catch (e: Exception) {
            Timber.e(e, "Error stopping Xray Core")
            false
        }
    }
    
    fun processPacket(data: ByteArray, length: Int): ByteArray {
        return try {
            if (isRunning) {
                nativeProcessPacket(data, length)
            } else {
                data
            }
        } catch (e: Exception) {
            Timber.e(e, "Error processing packet")
            data
        }
    }
}
