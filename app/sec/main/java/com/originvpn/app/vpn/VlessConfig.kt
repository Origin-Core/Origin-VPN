package com.originvpn.app.vpn

import org.json.JSONObject

/**
 * نگهدارنده‌ی پارامترهای یک کانفیگ VLESS به سبک Xray-core.
 * این کلاس فقط پارس و نگهداری تنظیمات است؛ اتصال واقعی و رمزنگاری پروتکل
 * توسط هسته‌ی Xray (کتابخانه‌ی AndroidLibXrayLite) انجام می‌شود، نه این کلاس.
 */
data class VlessConfig(
    val remarks: String,
    val address: String,
    val port: Int,
    val userId: String,
    val encryption: String,
    val network: String,
    val wsPath: String,
    val wsHost: String,
    val rawJson: String
) {
    companion object {
        /**
         * همان JSON کانفیگ کامل Xray (inbounds/outbounds/dns/routing) را می‌گیرد
         * و پارامترهای لازم برای UI را از آن استخراج می‌کند.
         * خودِ rawJson بدون تغییر به هسته‌ی Xray پاس داده می‌شود چون هسته
         * دقیقاً همین فرمت کانفیگ را انتظار دارد.
         */
        fun fromXrayJson(json: String): VlessConfig {
            val root = JSONObject(json)
            val remarks = root.optString("remarks", "Origin VPN Server")

            val outbounds = root.getJSONArray("outbounds")
            var proxyOutbound: JSONObject? = null
            for (i in 0 until outbounds.length()) {
                val ob = outbounds.getJSONObject(i)
                if (ob.optString("tag") == "proxy") {
                    proxyOutbound = ob
                    break
                }
            }
            requireNotNull(proxyOutbound) { "outbound با tag=proxy پیدا نشد" }

            val vnext = proxyOutbound.getJSONObject("settings")
                .getJSONArray("vnext").getJSONObject(0)
            val address = vnext.getString("address")
            val port = vnext.getInt("port")
            val user = vnext.getJSONArray("users").getJSONObject(0)
            val userId = user.getString("id")
            val encryption = user.optString("encryption", "none")

            val stream = proxyOutbound.getJSONObject("streamSettings")
            val network = stream.optString("network", "tcp")
            var wsPath = ""
            var wsHost = ""
            if (network == "ws" && stream.has("wsSettings")) {
                val ws = stream.getJSONObject("wsSettings")
                wsPath = ws.optString("path", "/")
                wsHost = ws.optJSONObject("headers")?.optString("Host", "") ?: ""
            }

            return VlessConfig(
                remarks = remarks,
                address = address,
                port = port,
                userId = userId,
                encryption = encryption,
                network = network,
                wsPath = wsPath,
                wsHost = wsHost,
                rawJson = json
            )
        }
    }
}
