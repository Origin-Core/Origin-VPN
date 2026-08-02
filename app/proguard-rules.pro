# Keep XRay core
-keep class com.xray.** { *; }
-keep class com.v2ray.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Keep Timber
-dontwarn timber.log.Timber

# Keep Compose
-keep class androidx.compose.** { *; }

# Optimizations
-optimizations !code/simplification/arithmetic
-optimizationpasses 5
-allowaccessmodification
-dontpreverify
