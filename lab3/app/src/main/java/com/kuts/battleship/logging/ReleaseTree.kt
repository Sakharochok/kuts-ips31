package com.kuts.battleship.logging

import android.util.Log
import timber.log.Timber

/**
 * Дерево логування для release-збірки: пропускає лише WARN та ERROR
 * і пише їх у системний лог під спільним тегом застосунку.
 */
class ReleaseTree : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority >= Log.WARN

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val fullTag = if (tag != null) "$TAG/$tag" else TAG
        Log.println(priority, fullTag, message)
        if (t != null) {
            Log.println(priority, fullTag, Log.getStackTraceString(t))
        }
    }

    private companion object {
        const val TAG = "Battleship"
    }
}
