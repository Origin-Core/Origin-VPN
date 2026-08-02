package com.origin.vpn.data.local.service

import android.content.Context
import com.v2ray.ang.V2RayCore
import timber.log.Timber

class XrayCore(private val context: Context) {
    
    companion object {
        init {
            // Load V2Ray Core library (از JitPack میاد)
            System.loadLibrary("v2ray")
        }
    }
    
    private var isRunning = false
    
    fun initialize(): Boolean {
        return try {
            V2RayCore.init(context)
            Timber.d("V2Ray Core initialized")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize V2Ray Core")
            false
        }
    }
    
    fun start(config: String): Boolean {
        return try {
            val result = V2RayCore.start(config)
            if (result) {
                isRunning = true
                Timber.d("V2Ray Core started")
            } else {
                Timber.e("Failed to start V2Ray Core")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "Error starting V2Ray Core")
            false
        }
    }
    
    fun stop(): Boolean {
        return try {
            if (isRunning) {
                val result = V2RayCore.stop()
                if (result) {
                    isRunning = false
                    Timber.d("V2Ray Core stopped")
                }
                result
            } else {
                true
            }
        } catch (e: Exception) {
            Timber.e(e, "Error stopping V2Ray Core")
            false
        }
    }
    
    fun processPacket(data: ByteArray, length: Int): ByteArray {
        return try {
            if (isRunning) {
                V2RayCore.process(data)
            } else {
                data
            }
        } catch (e: Exception) {
            Timber.e(e, "Error processing packet")
            data
        }
    }
}
