package com.origin.vpn.data.remote.model

import com.google.gson.annotations.SerializedName

data class VpnConfig(
    @SerializedName("v")
    val version: Int = 2,
    
    @SerializedName("ps")
    val name: String = "",
    
    @SerializedName("add")
    val address: String = "",
    
    @SerializedName("port")
    val port: Int = 443,
    
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("aid")
    val alterId: Int = 0,
    
    @SerializedName("net")
    val network: String = "ws", // ws, tcp, grpc
    
    @SerializedName("type")
    val type: String = "",
    
    @SerializedName("host")
    val host: String = "",
    
    @SerializedName("path")
    val path: String = "",
    
    @SerializedName("tls")
    val tls: String = "", // tls, xtls
    
    @SerializedName("sni")
    val sni: String = "",
    
    @SerializedName("security")
    val security: String = "auto",
    
    @SerializedName("encryption")
    val encryption: String = "none",
    
    @SerializedName("flow")
    val flow: String = "",
    
    @SerializedName("pbk")
    val publicKey: String = "",
    
    @SerializedName("sid")
    val shortId: String = "",
    
    @SerializedName("fp")
    val fingerprint: String = "",
    
    @SerializedName("headerType")
    val headerType: String = "",
    
    @SerializedName("enable")
    val enable: Boolean = true
)

data class VpnConfigList(
    @SerializedName("configs")
    val configs: List<VpnConfig>
)
