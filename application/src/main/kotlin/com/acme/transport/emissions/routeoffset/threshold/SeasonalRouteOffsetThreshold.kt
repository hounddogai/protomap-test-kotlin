package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal route offset threshold records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a seasonal route offset threshold is in its life cycle. */
enum class SeasonalRouteOffsetThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal route offset threshold as the route offset team keeps it. */
data class SeasonalRouteOffsetThreshold(
    var code: String,
    var title: String,
    var notes: String,
    var channel: String,
    var attempts: Int,
    var lane: String,
    var label: String,
    var category: String,
    var capacity: Int,
    var status: SeasonalRouteOffsetThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRouteOffsetThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRouteOffsetThresholdStatus): Boolean {
        when (status) {
            SeasonalRouteOffsetThresholdStatus.IDLE -> {
                when (next) {
                    SeasonalRouteOffsetThresholdStatus.WARMING, SeasonalRouteOffsetThresholdStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalRouteOffsetThresholdStatus.WARMING -> return next == SeasonalRouteOffsetThresholdStatus.DRAINING
            SeasonalRouteOffsetThresholdStatus.READY -> return next == SeasonalRouteOffsetThresholdStatus.STOPPED
            SeasonalRouteOffsetThresholdStatus.DRAINING -> {
                when (next) {
                    SeasonalRouteOffsetThresholdStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalRouteOffsetThresholdStatus) {
        if (!canMoveTo(next)) {
            error("seasonal route offset threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun effectiveValue(): Double = round((capacity.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): SeasonalRouteOffsetThreshold {
        return SeasonalRouteOffsetThreshold(
            code = code,
            title = title,
            notes = notes,
            channel = channel,
            attempts = attempts,
            lane = lane,
            label = label,
            category = category,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a seasonal route offset threshold in status idle. */
fun newSeasonalRouteOffsetThreshold(code: String, title: String): SeasonalRouteOffsetThreshold {
    return SeasonalRouteOffsetThreshold(
        code = code,
        title = title,
        notes = "",
        channel = "",
        attempts = 0,
        lane = "",
        label = "",
        category = "",
        capacity = 0,
        status = SeasonalRouteOffsetThresholdStatus.IDLE,
    )
}
