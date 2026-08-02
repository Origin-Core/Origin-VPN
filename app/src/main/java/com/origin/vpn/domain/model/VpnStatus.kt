package com.origin.vpn.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VpnStatus(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val ipAddress: String = "",
    val country: String = "",
    val city: String = "",
    val latency: Int = 0, // ms
    val ping: Int = 0, // ms
    val downloadSpeed: Long = 0, // bytes/s
    val uploadSpeed: Long = 0, // bytes/s
    val connectionTime: Long = 0, // seconds
    val trafficUsed: Long = 0, // bytes
    val errorMessage: String? = null
) : Parcelable
