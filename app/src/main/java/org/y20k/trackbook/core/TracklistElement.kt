/*
 * TracklistElement.kt
 * Implements the TracklistElement data class
 * A TracklistElement data about a Track
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


package org.y20k.trackbook.core

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import kotlinx.parcelize.Parcelize
import java.util.*


/*
 * TracklistElement data class
 */
@Keep
@Parcelize
data class TracklistElement(
    @Expose val id: Long,
    @Expose var name: String,
    @Expose val date: Date,
    @Expose val dateString: String,
    @Expose val duration: Long,
    @Expose val distance: Float,
    @Expose val trackUriString: String,
    @Expose val gpxUriString: String,
    @Expose var starred: Boolean = false
) : Parcelable {
}
