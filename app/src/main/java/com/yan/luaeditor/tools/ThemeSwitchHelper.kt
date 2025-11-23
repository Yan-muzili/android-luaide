package com.yan.luaeditor.tools

import android.app.Activity
import android.os.Build
import android.transition.TransitionInflater
import com.yan.luaide.R

object ThemeSwitchHelper {
    @JvmStatic
    fun installTransition(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val transition = TransitionInflater.from(activity)
                .inflateTransition(R.transition.fade)
            activity.window.apply {
                exitTransition   = transition
                enterTransition  = transition
                returnTransition = transition
                reenterTransition= transition
            }
        }
    }
}