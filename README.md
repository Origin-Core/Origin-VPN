# Origin VPN — پروژه‌ی اندروید

## این پکیج چیست؟
یک پروژه‌ی کامل Android Studio (Kotlin + Jetpack Compose) شامل:
- صفحه‌ی VPN با دکمه‌ی اتصال انیمیشن‌دار (پالس، رنگ متحرک) و نمایش اطلاعات سرور
- صفحه‌ی «مشخصات گوشی»: درصد باتری واقعی، وضعیت سلامت باتری، دما، حافظه‌ی
  داخلی استفاده‌شده/کل، RAM استفاده‌شده/کل، مدل و مشخصات دستگاه — همه از
  API واقعی اندروید خوانده می‌شوند (BatteryManager، StatFs، ActivityManager)
- `OriginVpnService` : یک `android.net.VpnService` واقعی که رابط TUN سیستم را
  بالا می‌آورد و کل ترافیک گوشی را می‌گیرد (permission رسمی VPN از کاربر گرفته می‌شود)
- پارسر کانفیگ VLESS/Xray که JSON دقیقاً همان‌طور که فرستادید را می‌خواند

## نکته‌ی مهم و صادقانه درباره‌ی پروتکل VLESS
پیاده‌سازی واقعی پروتکل VLESS (هندشیک، رمزنگاری، WebSocket transport) از صفر
در Kotlin کاری بسیار پیچیده، پرخطا و عملاً غیرمنطقی است — به همین دلیل **هیچ‌کدام**
از اپ‌های معروف مثل v2rayNG، NapsternetV یا Streisand این کار را نکرده‌اند.
همه‌ی آن‌ها هسته‌ی متن‌باز **Xray-core** (نوشته‌شده به Go) را به‌صورت یک
کتابخانه‌ی native (AAR) داخل اپ اندرویدی خود جاسازی می‌کنند.

این پروژه همان معماری واقعی صنعتی را پیاده کرده:
```
[TUN اندروید] --(بسته‌های IP خام)--> [tun2socks] --(SOCKS)--> [هسته‌ی Xray] --VLESS/WS--> [سرور شما]
```
کلاس‌های `XrayCoreBridge` و `Tun2SocksBridge` در `OriginVpnService.kt` محل
اتصال به این دو کتابخانه هستند و با کامنت `TODO` مشخص شده‌اند. برای فعال کردن
اتصال واقعی:

1. مخزن رسمی را بگیرید و AAR بسازید:
   `https://github.com/2dust/AndroidLibXrayLite`
   خروجی (`libv2ray.aar`) را در `app/libs/` بگذارید و در `app/build.gradle.kts`
   خط `implementation(files("libs/libv2ray.aar"))` را از کامنت خارج کنید.
2. برای لایه‌ی tun2socks از `hev-socks5-tunnel` یا `badvpn tun2socks`
   (نسخه‌ی کامپایل‌شده‌ی native برای arm64-v8a / armeabi-v7a / x86_64) استفاده کنید.
3. در `XrayCoreBridge.start()` متد `startLoop(xrayConfigJson)` کتابخانه را صدا بزنید
   و در `Tun2SocksBridge.run()` فایل‌دیسکریپتور TUN را به حلقه‌ی tun2socks بدهید.

با این دو کتابخانه، اتصال شما دقیقاً همان چیزی می‌شود که خواستید: یک VPN واقعی
و کاربردی با پروتکل VLESS، دقیقا با کانفیگی که دادید.

## دسترسی‌هایی که اپ می‌گیرد (و چرا)
| دسترسی | برای چه |
|---|---|
| `BIND_VPN_SERVICE` | ساخت رابط TUN واقعی (دیالوگ رسمی «اعتماد به این اپ برای VPN» را اندروید نشان می‌دهد) |
| `POST_NOTIFICATIONS` | نمایش نوتیفیکیشن دائمی «متصل» که سرویس فورگراند لازم دارد |
| `FOREGROUND_SERVICE` | زنده نگه‌داشتن اتصال VPN در پس‌زمینه |
| `ACCESS_NETWORK_STATE` / `INTERNET` | برقراری اتصال شبکه |

باتری/حافظه/مشخصات دستگاه نیازی به دسترسی خاص یا خطرناک ندارند — از API عمومی
سیستم خوانده می‌شوند.

## چک‌لیست نهایی — این مراحل را دقیقاً انجام بده تا واقعاً مثل یک VPN کار کند

`OriginVpnService.kt` الان کد واقعی صدا زدن API رسمی AndroidLibXrayLite رو
داره (`Libv2ray.newV2RayPoint`، `runLoop`، `stopLoop`، اینترفیس
`V2RayVPNServiceSupportsSet`) ولی به‌صورت کامنت، چون بدون خودِ فایل AAR
کامپایل نمی‌شود. قدم‌به‌قدم:

1. **گرفتن هسته‌ی Xray (AAR):**
   مخزن `github.com/2dust/AndroidLibXrayLite` رو کلون کن، طبق `README` خودش
   (نیاز به Go + gomobile داره) دستور بساز، خروجی `libv2ray.aar` رو بگیر.
   یا اگر عجله داری، از یک ریلیز از پیش کامپایل‌شده در گیت‌هاب همان پروژه
   استفاده کن (اگر ریلیز عمومی موجود بود).
   فایل رو بذار توی `app/libs/libv2ray.aar`.

2. **فعال کردن dependency:**
   در `app/build.gradle.kts` خط `implementation(files("libs/libv2ray.aar"))`
   رو از کامنت خارج کن.

3. **فعال کردن importها و متدها در `OriginVpnService.kt`:**
   سه خط import بالای فایل و بلوک `V2RayVPNServiceSupportsSet` رو از کامنت
   خارج کن، `: VpnService(), V2RayVPNServiceSupportsSet` رو در تعریف کلاس
   بذار، و بلوک واقعی `newV2RayPoint(...).runLoop(false)` رو در `startVpn()`
   فعال کن.

4. **گرفتن لایه‌ی tun2socks:**
   از `github.com/heiher/hev-socks5-tunnel` نسخه‌ی اندرویدش رو بگیر (یا از
   v2rayNG فورک کن)، فایل‌های `.so` رو برای هر ABI (arm64-v8a,
   armeabi-v7a, x86_64) توی `app/src/main/jniLibs/<ABI>/` بذار.
   در `Tun2SocksBridge` تابع native `TProxyStartService(configPath, fd)` رو
   با JNI وصل کن (`external fun` + `System.loadLibrary`).

5. **بیلد و تست روی گوشی واقعی** (نه شبیه‌ساز — VPN روی بعضی امولاتورها
   محدودیت داره). با `adb logcat | grep OriginVpnService` می‌تونی وضعیت
   اتصال رو ببینی.

بعد از این ۵ مرحله، اپ شما واقعاً یک VPN کامل با پروتکل VLESS است — دقیقاً
همان معماری‌ای که v2rayNG با میلیون‌ها نصب استفاده می‌کند.

## ساخت پروژه
1. پوشه را در Android Studio (Hedgehog یا جدیدتر) باز کنید.
2. AAR هسته‌ی Xray را طبق بالا اضافه کنید (بدون آن، اپ بالا می‌آید، رابط TUN
   ساخته می‌شود ولی ترافیک واقعی رمزگشایی/رمزگذاری نمی‌شود).
3. Run روی گوشی یا شبیه‌ساز.

## ساختار پروژه
```
OriginVPN/
├── app/src/main/java/com/originvpn/app/
│   ├── MainActivity.kt          # ناوبری + گرفتن مجوز VPN
│   ├── vpn/OriginVpnService.kt  # سرویس واقعی VPN + پل به Xray/tun2socks
│   ├── vpn/VlessConfig.kt       # پارس کانفیگ VLESS شما
│   ├── data/DefaultServer.kt    # همان JSON که فرستادید
│   ├── ui/VpnScreen.kt          # صفحه‌ی اصلی با انیمیشن اتصال
│   ├── ui/DeviceInfoScreen.kt   # باتری/حافظه/مشخصات با رینگ انیمیشن‌دار
│   └── util/DeviceInfoHelper.kt # خواندن واقعی وضعیت سیستم
```
