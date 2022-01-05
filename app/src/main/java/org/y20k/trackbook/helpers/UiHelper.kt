/*
 * UiHelper.kt
 * Implements the UiHelper object
 * A UiHelper provides helper methods for User Interface related tasks
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


package org.y20k.trackbook.helpers

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.y20k.trackbook.R
import org.y20k.trackbook.tracklist.TracklistAdapter


/*
 * UiHelper object
 */
object UiHelper {

    /* Define log tag */
    private val TAG: String = LogHelper.makeLogTag(UiHelper::class.java)


    /* Sets layout margins for given view in DP */
    fun setViewMargins(context: Context, view: View, left: Int = 0, right: Int = 0, top: Int= 0, bottom: Int = 0) {
        val scalingFactor: Float = context.resources.displayMetrics.density
        val l: Int = (left * scalingFactor).toInt()
        val r: Int = (right * scalingFactor).toInt()
        val t: Int = (top * scalingFactor).toInt()
        val b: Int = (bottom * scalingFactor).toInt()
        if (view.layoutParams is ViewGroup.MarginLayoutParams) {
            val p = view.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(l, t, r, b)
            view.requestLayout()
        }
    }


    /* Sets layout margins for given view in percent */
    fun setViewMarginsPercentage(context: Context, view: View, height: Int, width: Int, left: Int = 0, right: Int = 0, top: Int= 0, bottom: Int = 0) {
        val l: Int = ((width / 100.0f) * left).toInt()
        val r: Int = ((width / 100.0f) * right).toInt()
        val t: Int = ((height / 100.0f) * top).toInt()
        val b: Int = ((height / 100.0f) * bottom).toInt()
        setViewMargins(context, view, l, r, t, b)
    }


    /* Get the height of the system's top status bar */
    fun getStatusBarHeight(context: Context): Int {
        var result: Int = 0
        val resourceId: Int = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }


    /* Get scaling factor from display density */
    fun getDensityScalingFactor(context: Context): Float {
        return context.resources.displayMetrics.density
    }



    /*
     * Inner class: Callback that detects a left swipe
     * Credit: https://github.com/kitek/android-rv-swipe-delete/blob/master/app/src/main/java/pl/kitek/rvswipetodelete/SwipeToDeleteCallback.kt
     */
    abstract class SwipeToDeleteCallback(context: Context): ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_remove_circle_24dp)
        private val intrinsicWidth: Int = deleteIcon?.intrinsicWidth ?: 0
        private val intrinsicHeight: Int = deleteIcon?.intrinsicHeight ?: 0
        private val background: ColorDrawable = ColorDrawable()
        private val backgroundColor = context.resources.getColor(R.color.list_card_delete_background, null)
        private val clearPaint: Paint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            // do nothing
            return false
        }

        override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            // disable swipe for statistics element
            if (viewHolder is TracklistAdapter.ElementStatisticsViewHolder) return 0
            return super.getSwipeDirs(recyclerView, viewHolder)
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom - itemView.top
            val isCanceled = dX == 0f && !isCurrentlyActive

            if (isCanceled) {
                clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                return
            }

            // draw red delete background
            background.color = backgroundColor
            background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            background.draw(c)

            // calculate position of delete icon
            val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
            val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
            val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
            val deleteIconRight = itemView.right - deleteIconMargin
            val deleteIconBottom = deleteIconTop + intrinsicHeight

            // draw delete icon
            deleteIcon?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
            deleteIcon?.draw(c)

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
            c?.drawRect(left, top, right, bottom, clearPaint)
        }
    }
    /*
     * End of inner class
     */

}