package com.kuts.battleship

import android.app.Application
import com.kuts.battleship.logging.ReleaseTree
import timber.log.Timber

class BattleshipApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Debug: усі рівні з автоматичним тегом за іменем класу.
        // Release: лише попередження та помилки, без налагоджувального шуму.
        Timber.plant(if (BuildConfig.DEBUG) Timber.DebugTree() else ReleaseTree())
        Timber.i("Application started (debug=%s)", BuildConfig.DEBUG)
    }
}
