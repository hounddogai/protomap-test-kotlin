package com.acme.transport.terminal.crossdock.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal cross dock segment is in its life cycle. */
enum class SeasonalCrossDockSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal cross dock segment as the cross dock team keeps it. */
data class SeasonalCrossDockSegment(
    var code: String,
    var label: String,
    var margin: Double,
    var pinned: Boolean,
    var lane: String,
    var durationMinutes: Int,
    var distanceKm: Double,
    var quantity: Int,
    var priority: Int,
    var batchSize: Int,
    var status: SeasonalCrossDockSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCrossDockSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCrossDockSegmentStatus): Boolean {
        when (status) {
            SeasonalCrossDockSegmentStatus.NEW -> return next == SeasonalCrossDockSegmentStatus.RUNNING
            SeasonalCrossDockSegmentStatus.QUEUED -> return next == SeasonalCrossDockSegmentStatus.RUNNING || next == SeasonalCrossDockSegmentStatus.FAILED
            SeasonalCrossDockSegmentStatus.RUNNING -> {
                when (next) {
                    SeasonalCrossDockSegmentStatus.FAILED -> return true
                }
                return false
            }
            SeasonalCrossDockSegmentStatus.DONE -> {
                when (next) {
                    SeasonalCrossDockSegmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalCrossDockSegmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal cross dock segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedVolume(): Double = round((distanceKm + quantity.toDouble()) * 100) / 100

    fun snapshot(): SeasonalCrossDockSegment {
        return SeasonalCrossDockSegment(
            code = code,
            label = label,
            margin = margin,
            pinned = pinned,
            lane = lane,
            durationMinutes = durationMinutes,
            distanceKm = distanceKm,
            quantity = quantity,
            priority = priority,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a seasonal cross dock segment in status new. */
fun newSeasonalCrossDockSegment(code: String, label: String): SeasonalCrossDockSegment {
    return SeasonalCrossDockSegment(
        code = code,
        label = label,
        margin = 0.0,
        pinned = false,
        lane = "",
        durationMinutes = 0,
        distanceKm = 0.0,
        quantity = 0,
        priority = 0,
        batchSize = 0,
        status = SeasonalCrossDockSegmentStatus.NEW,
    )
}
