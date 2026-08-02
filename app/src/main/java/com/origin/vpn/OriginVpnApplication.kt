package com.origin.vpn

import android.app.Application
import com.origin.vpn.utils.AppContextProvider
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class OriginVpnApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize AppContextProvider
        AppContextProvider.init(this)
        
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
