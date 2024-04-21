package com.yucox.whatthenads.Util

import android.content.pm.ActivityInfo
import android.view.WindowInsets

object SynchroneScreen{
    val FULLSCREEN = WindowInsets.Type.statusBars()
    val NAVBAR = WindowInsets.Type.navigationBars()
    const val PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    const val LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
}

enum class Weekday {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}
