/*
 * Trackbook.kt
 * Implements the Trackbook class
 * Trackbook is the base Application class that sets up day and night theme
 *
 * This file is part of
 * TRACKBOOK - Movement Recorder for Android
 *
 * Copyright (c) 2016-22 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 *
 * Trackbook uses osmdroid - OpenStreetMap-Tools for Android
 * https://github.com/osmdroid/osmdroid
 */



package org.y20k.trackbook

import android.app.Application
import com.google.android.material.color.DynamicColors
import org.y20k.trackbook.helpers.AppThemeHelper
import org.y20k.trackbook.helpers.LogHelper
import org.y20k.trackbook.helpers.PreferencesHelper
import org.y20k.trackbook.helpers.PreferencesHelper.initPreferences


/*
 * Trackbook.class
 */
class Trackbook: Application() {


    /* Define log tag */
    private val TAG: String = LogHelper.makeLogTag(Trackbook::class.java)


    /* Implements onCreate */
    override fun onCreate() {
        super.onCreate()
        LogHelper.v(TAG, "Trackbook application started.")
        DynamicColors.applyToActivitiesIfAvailable(this);
        // initialize single sharedPreferences object when app is launched
        initPreferences()
        // set Dark / Light theme state
        AppThemeHelper.setTheme(PreferencesHelper.loadThemeSelection())
    }


    /* Implements onTerminate */
    override fun onTerminate() {
        super.onTerminate()
        LogHelper.v(TAG, "Trackbook application terminated.")
    }

}