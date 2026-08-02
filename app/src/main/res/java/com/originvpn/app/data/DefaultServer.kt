package com.originvpn.app.data

/**
 * کانفیگ پیش‌فرض سرور به سبک Xray-core / VLESS.
 * این همان JSON‌ای است که کاربر داده، با پاک‌سازی لینک‌های مارک‌داون اضافی
 * (لینک‌های search.eitaa.com که کلاینت تلگرام/ایتا به دور آی‌پی و دامنه‌ها
 * اضافه کرده بود) تا یک JSON معتبر و قابل‌پارس باشد.
 *
 * نکته: این فایل را در پروژه‌ی واقعی هاردکد نکنید — کاربر باید بتواند
 * کانفیگ‌های خودش را با اسکن QR یا Paste از کلیپ‌بورد اضافه کند. اینجا فقط
 * برای تست اولیه‌ی جریان اتصال قرار داده شده.
 */
object DefaultServer {
    const val CONFIG_JSON = """
    {
      "remarks": "PS-AR-DE-1",
      "log": { "loglevel": "warning" },
      "inbounds": [
        {
          "tag": "socks",
          "port": 10808,
          "protocol": "socks",
          "settings": { "auth": "noauth", "udp": true, "userLevel": 8 },
          "sniffing": { "enabled": true, "destOverride": ["http", "tls"], "routeOnly": false }
        }
      ],
      "outbounds": [
        {
          "tag": "proxy",
          "protocol": "vless",
          "settings": {
            "vnext": [
              {
                "address": "185.143.233.5",
                "port": 80,
                "users": [
                  {
                    "id": "1ad54dee-617b-4bcf-b6ed-64d41ea88836",
                    "level": 8,
                    "encryption": "mlkem768x25519plus.native.0rtt.1TsRuWZdAHrU7z_ervQFaDusaDI9h2Cyn5HI-DoR5jQ"
                  }
                ]
              }
            ]
          },
          "streamSettings": {
            "network": "ws",
            "wsSettings": {
              "path": "/gamescore?ed=128",
              "headers": { "Host": "de1-f96a03cd07-loadbalancer.apps.ir-central1.arvancaas.ir" }
            }
          },
          "mux": { "enabled": false, "concurrency": -1, "xudpConcurrency": 8, "xudpProxyUDP443": "" }
        },
        {
          "tag": "direct",
          "protocol": "freedom",
          "settings": { "domainStrategy": "UseIP" },
          "mux": { "enabled": false, "concurrency": 8, "xudpConcurrency": 8, "xudpProxyUDP443": "" }
        },
        {
          "tag": "block",
          "protocol": "blackhole",
          "settings": { "response": { "type": "http" } },
          "mux": { "enabled": false, "concurrency": 8, "xudpConcurrency": 8, "xudpProxyUDP443": "" }
        }
      ],
      "dns": {
        "servers": ["1.1.1.1"],
        "hosts": {
          "domain:googleapis.cn": "googleapis.com",
          "dns.alidns.com": ["223.5.5.5", "223.6.6.6", "2400:3200::1", "2400:3200:baba::1"],
          "one.one.one.one": ["1.1.1.1", "1.0.0.1", "2606:4700:4700::1111", "2606:4700:4700::1001"],
          "dot.pub": ["1.12.12.12", "120.53.53.53"],
          "dns.google": ["8.8.8.8", "8.8.4.4", "2001:4860:4860::8888", "2001:4860:4860::8844"],
          "dns.quad9.net": ["9.9.9.9", "149.112.112.112", "2620:fe::fe", "2620:fe::9"],
          "common.dot.dns.yandex.net": ["77.88.8.8", "77.88.8.1", "2a02:6b8::feed:0ff", "2a02:6b8:0:1::feed:0ff"]
        }
      },
      "routing": {
        "domainStrategy": "IPIfNonMatch",
        "rules": [
          { "type": "field", "ip": ["1.1.1.1"], "outboundTag": "proxy", "port": "53" },
          { "type": "field", "ip": ["223.5.5.5"], "outboundTag": "direct", "port": "53" }
        ]
      }
    }
    """
}
