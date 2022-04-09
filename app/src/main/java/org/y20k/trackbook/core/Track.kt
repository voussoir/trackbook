/*
 * Track.kt
 * Implements the Track data class
 * A Track stores a list of WayPoints
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

import android.content.Context
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import java.util.*
import kotlin.random.Random
import kotlinx.parcelize.Parcelize
import org.y20k.trackbook.Keys
import org.y20k.trackbook.helpers.DateTimeHelper

/*
 * Track data class
 */
@Keep
@Parcelize
data class Track (@Expose val id: Long = make_random_id(),
                  @Expose var trackFormatVersion: Int = Keys.CURRENT_TRACK_FORMAT_VERSION,
                  @Expose val wayPoints: MutableList<WayPoint> = mutableListOf<WayPoint>(),
                  @Expose var distance: Float = 0f,
                  @Expose var duration: Long = 0L,
                  @Expose var recordingPaused: Long = 0L,
                  @Expose var stepCount: Float = 0f,
                  @Expose var recordingStart: Date = GregorianCalendar.getInstance().time,
                  @Expose var recordingStop: Date = recordingStart,
                  @Expose var maxAltitude: Double = 0.0,
                  @Expose var minAltitude: Double = 0.0,
                  @Expose var positiveElevation: Double = 0.0,
                  @Expose var negativeElevation: Double = 0.0,
                  @Expose var trackUriString: String = String(),
                  @Expose var gpxUriString: String = String(),
                  @Expose var latitude: Double = Keys.DEFAULT_LATITUDE,
                  @Expose var longitude: Double = Keys.DEFAULT_LONGITUDE,
                  @Expose var zoomLevel: Double = Keys.DEFAULT_ZOOM_LEVEL,
                  @Expose var name: String = String()): Parcelable
{
    /* Creates a TracklistElement */
    fun toTracklistElement(context: Context): TracklistElement {
        val readableDateString: String = DateTimeHelper.convertToReadableDate(recordingStart)
        val readableDurationString: String = DateTimeHelper.convertToReadableTime(context, duration)
        return TracklistElement(
            id = id,
            name = name,
            date = recordingStart,
            dateString = readableDateString,
            distance = distance,
            duration = duration,
            trackUriString = trackUriString,
            gpxUriString = gpxUriString,
            starred = false
        )
    }
}

fun make_random_id(): Long
{
    return (Random.nextBits(31).toLong() shl 32) + Random.nextBits(32)
}
