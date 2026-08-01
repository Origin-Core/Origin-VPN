package com.originvpn.app.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.originvpn.app.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
// import libv2ray.Libv2ray
// import libv2ray.V2RayPoint
// import libv2ray.V2RayVPNServiceSupportsSet
// ^ این سه import وقتی فعال می‌شوند که libv2ray.aar را به app/libs اضافه کرده باشید.

/**
 * سرویس واقعی VPN در سطح سیستم‌عامل اندروید.
 *
 * این کلاس مستقیماً اینترفیس V2RayVPNServiceSupportsSet را پیاده می‌کند تا
 * خودِ AndroidLibXrayLite بتواند از طریق Setup/Prepare/Protect/Shutdown با
 * سیستم اندروید (که فقط VpnService بهش دسترسی دارد) صحبت کند.
 * این دقیقاً همان الگویی است که v2rayNG در V2RayVpnService.kt استفاده می‌کند.
 */
class OriginVpnService : VpnService() /* , V2RayVPNServiceSupportsSet */ {

    companion object {
        const val ACTION_CONNECT = "com.originvpn.app.CONNECT"
        const val ACTION_DISCONNECT = "com.originvpn.app.DISCONNECT"
        const val EXTRA_CONFIG_JSON = "config_json"
        private const val CHANNEL_ID = "origin_vpn_channel"
        private const val NOTIF_ID = 1
        private const val TAG = "OriginVpnService"

        @Volatile
        var isRunning: Boolean = false
            private set
    }

    private var tunFd: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var currentConfig: VlessConfig? = null

    // نمونه‌ی واقعی V2RayPoint از هسته‌ی Xray (بعد از اضافه کردن AAR، کامنت را بردارید)
    // private var v2rayPoint: V2RayPoint? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val json = intent.getStringExtra(EXTRA_CONFIG_JSON) ?: return START_NOT_STICKY
                startVpn(VlessConfig.fromXrayJson(json))
            }
            ACTION_DISCONNECT -> stopVpn()
        }
        return START_STICKY
    }

    // ---------------------------------------------------------------------
    // مرحله ۱: راه‌اندازی هسته‌ی Xray (بدون این‌که خودش TUN بسازد)
    // ---------------------------------------------------------------------
    private fun startVpn(config: VlessConfig) {
        currentConfig = config
        startForeground(NOTIF_ID, buildNotification("در حال برقراری اتصال به ${config.remarks}"))

        serviceScope.launch {
            try {
                /* --- کد واقعی (بعد از اضافه کردن AAR این بلوک را از کامنت خارج کنید) ---

                Libv2ray.initV2Env(filesDir.absolutePath) // مسیر geoip.dat / geosite.dat

                v2rayPoint = Libv2ray.newV2RayPoint(this@OriginVpnService, false).apply {
                    configureFileContent = config.rawJson   // همان JSON کامل شما
                    domainName = "${config.address}:${config.port}"
                    enableLocalDNS = false
                }
                v2rayPoint?.runLoop(false)   // اینجا هسته‌ی Xray واقعاً هندشیک VLESS را با سرور انجام می‌دهد

                --------------------------------------------------------------- */

                // مرحله ۲: بعد از این‌که هسته آماده شد، TUN را می‌سازیم (این بخش الان واقعی و کامل است)
                establishTun(config)

            } catch (e: Exception) {
                Log.e(TAG, "vpn start failed", e)
                updateNotification("اتصال ناموفق بود")
                stopVpn()
            }
        }
    }

    private fun establishTun(config: VlessConfig) {
        val builder = Builder()
            .setSession("Origin VPN")
            .addAddress("10.10.10.2", 32)
            .addDnsServer("1.1.1.1")
            .addRoute("0.0.0.0", 0)
            .setMtu(1500)
            // اپ خودمان را از تونل مستثنی می‌کنیم تا با خودش لوپ نشود
            .addDisallowedApplication(packageName)

        tunFd = builder.establish()
        isRunning = true
        updateNotification("متصل به ${config.remarks}")

        // مرحله ۳: هدایت بسته‌های خام TUN به SOCKS محلی هسته‌ی Xray (پورت 10808)
        tunFd?.let { fd ->
            Tun2SocksBridge.run(fd, filesDir.absolutePath, "127.0.0.1", 10808)
        }
    }

    private fun stopVpn() {
        serviceScope.launch {
            Tun2SocksBridge.stop()
            // v2rayPoint?.stopLoop()
            // v2rayPoint = null
            tunFd?.close()
            tunFd = null
            isRunning = false
        }
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    override fun onRevoke() {
        stopVpn()
        super.onRevoke()
    }

    // ---------------------------------------------------------------------
    // پیاده‌سازی V2RayVPNServiceSupportsSet — اینترفیسی که خودِ هسته‌ی Xray
    // صدا می‌زند تا با لایه‌ی VpnService اندروید هماهنگ شود.
    // وقتی import های بالا را باز کردید، این متدها را override کنید:
    // ---------------------------------------------------------------------
    /*
    override fun shutdown(): Long {
        stopVpn()
        return 0
    }

    override fun prepare(): Long {
        val intent = VpnService.prepare(this)
        return if (intent != null) -1 else 0
    }

    override fun protect(fd: Long): Boolean {
        // این تابع باید سوکت خام هسته‌ی Xray را از رفتن داخل خودِ تونل معاف کند
        // وگرنه ترافیک هسته دوباره وارد TUN می‌شود و لوپ بی‌نهایت ایجاد می‌کند.
        return protect(fd.toInt())
    }

    override fun onEmitStatus(code: Long, msg: String?): Long {
        Log.d(TAG, "xray status: $code $msg")
        return 0
    }

    override fun setup(config: String?): Long {
        // این نسخه از establishTun() به‌جای اینجا صدا زده می‌شود؛ اگر از نسخه‌ی
        // دیگری از AndroidLibXrayLite استفاده کردید که خودش setup را صدا می‌زند
        // (بعضی فورک‌ها این‌طوری‌اند)، establishTun(currentConfig!!) را اینجا بگذارید
        // و از startVpn() حذفش کنید.
        return tunFd?.fd?.toLong() ?: -1
    }
    */

    private fun buildNotification(text: String): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Origin VPN", NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Origin VPN")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIF_ID, buildNotification(text))
    }
}

/**
 * پل ارتباطی به لایه‌ی tun2socks (پیشنهاد: hev-socks5-tunnel).
 * این کتابخانه بسته‌های خام IP روی فایل‌دیسکریپتور TUN را می‌خواند و به‌صورت
 * SOCKS5 به 127.0.0.1:10808 (جایی که هسته‌ی Xray گوش می‌دهد) می‌فرستد.
 *
 * قدم بعدی شما: از https://github.com/heiher/hev-socks5-tunnel نسخه‌ی
 * اندرویدش (libhev-socks5-tunnel.so برای هر ABI) را بگیرید و اینجا JNI را
 * وصل کنید. امضای متد native معمولاً چیزی شبیه این است:
 *   external fun TProxyStartService(configPath: String, fd: Int)
 *   external fun TProxyStopService()
 */
object Tun2SocksBridge {

    // system("hev-socks5-tunnel") -> بعد از اضافه کردن .so های native این خط را باز کنید
    // init { System.loadLibrary("hev-socks5-tunnel") }

    // external fun nativeStart(configPath: String, fd: Int)
    // external fun nativeStop()

    fun run(tunFd: ParcelFileDescriptor, filesDir: String, socksHost: String, socksPort: Int) {
        // فایل کانفیگ yaml مورد نیاز hev-socks5-tunnel را می‌سازیم:
        val configFile = java.io.File(filesDir, "tun2socks.yaml")
        configFile.writeText(
            """
            tunnel:
              mtu: 1500
            socks5:
              address: '$socksHost'
              port: $socksPort
              udp: 'udp'
            """.trimIndent()
        )
        // TODO: nativeStart(configFile.absolutePath, tunFd.fd)
    }

    fun stop() {
        // TODO: nativeStop()
    }
}
