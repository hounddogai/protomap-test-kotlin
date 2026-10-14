package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer segment is in its life cycle. */
enum class TrailerSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A trailer segment as the trailer team keeps it. */
data class TrailerSegment(
    var code: String,
    var title: String,
    var label: String,
    var floorValue: Double,
    var stopCount: Int,
    var version: Int,
    var tags: MutableList<String>,
    var status: TrailerSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerSegmentStatus): Boolean {
        when (status) {
            TrailerSegmentStatus.IDLE -> {
                when (next) {
                    TrailerSegmentStatus.READY, TrailerSegmentStatus.STOPPED -> return true
                }
                return false
            }
            TrailerSegmentStatus.WARMING -> return next == TrailerSegmentStatus.READY || next == TrailerSegmentStatus.DRAINING
            TrailerSegmentStatus.READY -> {
                when (next) {
                    TrailerSegmentStatus.DRAINING, TrailerSegmentStatus.STOPPED -> return true
                }
                return false
            }
            TrailerSegmentStatus.DRAINING -> {
                when (next) {
                    TrailerSegmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerSegmentStatus) {
        if (!canMoveTo(next)) {
            error("trailer segment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedVolume(): Double = round((version.toDouble() + floorValue) * 100) / 100

    fun snapshot(): TrailerSegment {
        return TrailerSegment(
            code = code,
            title = title,
            label = label,
            floorValue = floorValue,
            stopCount = stopCount,
            version = version,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a trailer segment in status idle. */
fun newTrailerSegment(code: String, title: String): TrailerSegment {
    return TrailerSegment(
        code = code,
        title = title,
        label = "",
        floorValue = 0.0,
        stopCount = 0,
        version = 0,
        tags = mutableListOf<String>(),
        status = TrailerSegmentStatus.IDLE,
    )
}
