/*
 * TrackFragmentLayoutHolder.kt
 * Implements the TrackFragmentLayoutHolder class
 * A TrackFragmentLayoutHolder hold references to the main views of a track fragment
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


package org.y20k.trackbook.ui

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.api.IGeoPoint
import org.osmdroid.api.IMapController
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay
import org.y20k.trackbook.Keys
import org.y20k.trackbook.R
import org.y20k.trackbook.core.Track
import org.y20k.trackbook.helpers.*
import kotlin.math.roundToInt


/*
 * TrackFragmentLayoutHolder class
 */
//data class TrackFragmentLayoutHolder(private var context: Context, private var markerListener: MapOverlayHelper.MarkerListener, private var inflater: LayoutInflater, private var statusBarHeight: Int, private var container: ViewGroup?, var track: Track): MapListener { TODO REMOVE
data class TrackFragmentLayoutHolder(private var context: Context, private var markerListener: MapOverlayHelper.MarkerListener, private var inflater: LayoutInflater, private var container: ViewGroup?, var track: Track): MapListener {

    /* Define log tag */
    private val TAG: String = LogHelper.makeLogTag(TrackFragmentLayoutHolder::class.java)


    /* Main class variables */
    val rootView: View
    val shareButton: ImageButton
    val deleteButton: ImageButton
    val editButton: ImageButton
    val trackNameView: MaterialTextView
    private val mapView: MapView
    private var trackSpecialMarkersOverlay: ItemizedIconOverlay<OverlayItem>?
    private var trackOverlay: SimpleFastPointOverlay?
    private var controller: IMapController
    //private var zoomLevel: Double
    private val statisticsSheetBehavior: BottomSheetBehavior<View>
    private val statisticsSheet: NestedScrollView
    private val statisticsView: View
    private val distanceView: MaterialTextView
    private val stepsTitleView: MaterialTextView
    private val stepsView: MaterialTextView
    private val waypointsView: MaterialTextView
    private val durationView: MaterialTextView
    private val velocityView: MaterialTextView
    private val recordingStartView: MaterialTextView
    private val recordingStopView: MaterialTextView
    private val recordingPausedView: MaterialTextView
    private val recordingPausedLabelView: MaterialTextView
    private val maxAltitudeView: MaterialTextView
    private val minAltitudeView: MaterialTextView
    private val positiveElevationView: MaterialTextView
    private val negativeElevationView: MaterialTextView
    private val elevationDataViews: Group
    private val trackManagementViews: Group
    private val useImperialUnits: Boolean


    /* Init block */
    init {
        // find views
        rootView = inflater.inflate(R.layout.fragment_track, container, false)
        mapView = rootView.findViewById(R.id.map)
        shareButton = rootView.findViewById(R.id.save_button)
        deleteButton = rootView.findViewById(R.id.delete_button)
        editButton = rootView.findViewById(R.id.edit_button)
        trackNameView = rootView.findViewById(R.id.statistics_track_name_headline)

        // basic map setup
        controller = mapView.controller
        mapView.addMapListener(this)
        mapView.isTilesScaledToDpi = true
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
        controller.setCenter(GeoPoint(track.latitude, track.longitude))
        controller.setZoom(track.zoomLevel)

        // get views for statistics sheet
        statisticsSheet = rootView.findViewById(R.id.statistics_sheet)
        statisticsView = rootView.findViewById(R.id.statistics_view)
        distanceView = rootView.findViewById(R.id.statistics_data_distance)
        stepsTitleView = rootView.findViewById(R.id.statistics_p_steps)
        stepsView = rootView.findViewById(R.id.statistics_data_steps)
        waypointsView = rootView.findViewById(R.id.statistics_data_waypoints)
        durationView = rootView.findViewById(R.id.statistics_data_duration)
        velocityView = rootView.findViewById(R.id.statistics_data_velocity)
        recordingStartView = rootView.findViewById(R.id.statistics_data_recording_start)
        recordingStopView = rootView.findViewById(R.id.statistics_data_recording_stop)
        recordingPausedLabelView = rootView.findViewById(R.id.statistics_p_recording_paused)
        recordingPausedView = rootView.findViewById(R.id.statistics_data_recording_paused)
        maxAltitudeView = rootView.findViewById(R.id.statistics_data_max_altitude)
        minAltitudeView = rootView.findViewById(R.id.statistics_data_min_altitude)
        positiveElevationView = rootView.findViewById(R.id.statistics_data_positive_elevation)
        negativeElevationView = rootView.findViewById(R.id.statistics_data_negative_elevation)
        elevationDataViews = rootView.findViewById(R.id.elevation_data)
        trackManagementViews = rootView.findViewById(R.id.management_icons)

        // get measurement unit system
        useImperialUnits = PreferencesHelper.loadUseImperialUnits()

        // set dark map tiles, if necessary
        if (AppThemeHelper.isDarkModeOn(context as Activity)) {
            mapView.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }

        // add compass to map
        val compassOverlay = CompassOverlay(context, InternalCompassOrientationProvider(context), mapView)
        compassOverlay.enableCompass()
        compassOverlay.setCompassCenter(36f, 36f)
//        compassOverlay.setCompassCenter(36f, 36f + (statusBarHeight / UiHelper.getDensityScalingFactor(context))) TODO REMOVE
        mapView.overlays.add(compassOverlay)

        // create map overlay
        val mapOverlayHelper: MapOverlayHelper = MapOverlayHelper(markerListener)
        trackOverlay = mapOverlayHelper.createTrackOverlay(context, track, Keys.STATE_TRACKING_NOT_STARTED)
        trackSpecialMarkersOverlay = mapOverlayHelper.createSpecialMakersTrackOverlay(context, track, Keys.STATE_TRACKING_NOT_STARTED, displayStartEndMarker = true)
        if (track.wayPoints.isNotEmpty()) {
            mapView.overlays.add(trackSpecialMarkersOverlay)
            mapView.overlays.add(trackOverlay)
        }

        // set up and show statistics sheet
        statisticsSheetBehavior = BottomSheetBehavior.from<View>(statisticsSheet)
        statisticsSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        statisticsSheetBehavior.addBottomSheetCallback(getStatisticsSheetCallback())
        setupStatisticsViews()
    }


    /* Updates map overlay */
    fun updateTrackOverlay() {
        if (trackOverlay != null) {
            mapView.overlays.remove(trackOverlay)
        }
        if (trackSpecialMarkersOverlay != null) {
            mapView.overlays.remove(trackSpecialMarkersOverlay)
        }
        if (track.wayPoints.isNotEmpty()) {
            val mapOverlayHelper: MapOverlayHelper = MapOverlayHelper(markerListener)
            trackOverlay = mapOverlayHelper.createTrackOverlay(context, track, Keys.STATE_TRACKING_NOT_STARTED)
            trackSpecialMarkersOverlay = mapOverlayHelper.createSpecialMakersTrackOverlay(context, track, Keys.STATE_TRACKING_NOT_STARTED, displayStartEndMarker = true)
            mapView.overlays.add(trackOverlay)
            mapView.overlays.add(trackSpecialMarkersOverlay)
        }
        // save track
        CoroutineScope(Dispatchers.IO).launch { FileHelper.saveTrackSuspended(track, true) }
    }


    /* Saves zoom level and center of this map */
    fun saveViewStateToTrack() {
        if (track.latitude != 0.0 && track.longitude != 0.0) {
            CoroutineScope(Dispatchers.IO).launch { FileHelper.saveTrackSuspended(track, false) }
        }
    }


    /* Sets up the statistics sheet */
    private fun setupStatisticsViews() {

        // get step count string - hide step count if not available
        val steps: String
        if (track.stepCount == -1f) {
            steps = context.getString(R.string.statistics_sheet_p_steps_no_pedometer)
            stepsTitleView.isGone = true
            stepsView.isGone = true
        }
        else {
            steps = track.stepCount.roundToInt().toString()
            stepsTitleView.isVisible = true
            stepsView.isVisible = true
        }

        // populate views
        trackNameView.text = track.name
        distanceView.text = LengthUnitHelper.convertDistanceToString(track.length, useImperialUnits)
        stepsView.text = steps
        waypointsView.text = track.wayPoints.size.toString()
        durationView.text = DateTimeHelper.convertToReadableTime(context, track.duration)
        velocityView.text = LengthUnitHelper.convertToVelocityString(track.duration, track.recordingPaused, track.length, useImperialUnits)
        recordingStartView.text = DateTimeHelper.convertToReadableDateAndTime(track.recordingStart)
        recordingStopView.text = DateTimeHelper.convertToReadableDateAndTime(track.recordingStop)
        maxAltitudeView.text = LengthUnitHelper.convertDistanceToString(track.maxAltitude, useImperialUnits)
        minAltitudeView.text = LengthUnitHelper.convertDistanceToString(track.minAltitude, useImperialUnits)
        positiveElevationView.text = LengthUnitHelper.convertDistanceToString(track.positiveElevation, useImperialUnits)
        negativeElevationView.text = LengthUnitHelper.convertDistanceToString(track.negativeElevation, useImperialUnits)

        // show / hide recording pause
        if (track.recordingPaused != 0L) {
            recordingPausedLabelView.isVisible = true
            recordingPausedView.isVisible = true
            recordingPausedView.text = DateTimeHelper.convertToReadableTime(context, track.recordingPaused)
        } else {
            recordingPausedLabelView.isGone = true
            recordingPausedView.isGone = true
        }

        // inform user about possible accuracy issues with altitude measurements
        elevationDataViews.referencedIds.forEach { id ->
            (rootView.findViewById(id) as View).setOnClickListener{
                Toast.makeText(context, R.string.toast_message_elevation_info, Toast.LENGTH_LONG).show()
            }
        }
        // make track name on statistics sheet clickable
        trackNameView.setOnClickListener {
            toggleStatisticsSheetVisibility()
        }
    }


    /* Shows/hides the statistics sheet */
    private fun toggleStatisticsSheetVisibility() {
        when (statisticsSheetBehavior.state) {
            BottomSheetBehavior.STATE_EXPANDED -> statisticsSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            else -> statisticsSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }


    /* Defines the behavior of the statistics sheet  */
    private fun getStatisticsSheetCallback(): BottomSheetBehavior.BottomSheetCallback {
        return object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        trackManagementViews.isVisible = true
                        shareButton.isGone = true
                        // bottomSheet.setPadding(0,24,0,0)
                    }
                    else -> {
                        trackManagementViews.isGone = true
                        shareButton.isVisible = true
                        // bottomSheet.setPadding(0,0,0,0)
                    }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset < 0.125f) {
                    trackManagementViews.isGone = true
                    shareButton.isVisible = true
                } else {
                    trackManagementViews.isVisible = true
                    shareButton.isGone = true
                }
            }
        }
    }


    /* Overrides onZoom from MapListener */
    override fun onZoom(event: ZoomEvent?): Boolean {
        if (event == null) {
            return false
        } else {
            track.zoomLevel = event.zoomLevel
            return true
        }
    }


    /* Overrides onScroll from MapListener */
    override fun onScroll(event: ScrollEvent?): Boolean {
        if (event == null) {
            return false
        } else {
            val center: IGeoPoint = mapView.mapCenter
            track.latitude = center.latitude
            track.longitude = center.longitude
            return true
        }
    }

}

