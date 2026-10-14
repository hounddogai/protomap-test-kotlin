package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal dock door segment is in its life cycle. */
enum class SeasonalDockDoorSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a seasonal dock door segment and its current status. */
data class SeasonalDockDoorSegment(
    var code: String,
    var title: String,
    var region: String,
    var name: String,
    var floorValue: Double,
    var trailerId: String,
    var endsAt: Int,
    var windowDays: Int,
    var attributes: MutableMap<String, String>,
    var status: SeasonalDockDoorSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDockDoorSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDockDoorSegmentStatus): Boolean {
        when (status) {
            SeasonalDockDoorSegmentStatus.IDLE -> {
                when (next) {
                    SeasonalDockDoorSegmentStatus.WARMING, SeasonalDockDoorSegmentStatus.DRAINING -> return true
                }
                return false
            }
            SeasonalDockDoorSegmentStatus.WARMING -> {
                when (next) {
                    SeasonalDockDoorSegmentStatus.READY, SeasonalDockDoorSegmentStatus.DRAINING -> return true
                }
                return false
            }
            SeasonalDockDoorSegmentStatus.READY -> return next == SeasonalDockDoorSegmentStatus.DRAINING || next == SeasonalDockDoorSegmentStatus.STOPPED
            SeasonalDockDoorSegmentStatus.DRAINING -> return next == SeasonalDockDoorSegmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SeasonalDockDoorSegmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal dock door segment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round(floorValue * windowDays.toDouble() * 100) / 100

    fun snapshot(): SeasonalDockDoorSegment {
        return SeasonalDockDoorSegment(
            code = code,
            title = title,
            region = region,
            name = name,
            floorValue = floorValue,
            trailerId = trailerId,
            endsAt = endsAt,
            windowDays = windowDays,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a seasonal dock door segment in status idle. */
fun newSeasonalDockDoorSegment(code: String, title: String): SeasonalDockDoorSegment {
    return SeasonalDockDoorSegment(
        code = code,
        title = title,
        region = "",
        name = "",
        floorValue = 0.0,
        trailerId = "",
        endsAt = 0,
        windowDays = 0,
        attributes = mutableMapOf<String, String>(),
        status = SeasonalDockDoorSegmentStatus.IDLE,
    )
}
