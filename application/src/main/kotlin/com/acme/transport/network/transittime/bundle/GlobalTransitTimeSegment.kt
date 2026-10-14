package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global transit time segment is in its life cycle. */
enum class GlobalTransitTimeSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A global transit time segment as the transit time team keeps it. */
data class GlobalTransitTimeSegment(
    var code: String,
    var label: String,
    var threshold: Double,
    var retries: Int,
    var archived: Boolean,
    var source: String,
    var vehicleId: String,
    var rate: Double,
    var status: GlobalTransitTimeSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTransitTimeSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTransitTimeSegmentStatus): Boolean {
        when (status) {
            GlobalTransitTimeSegmentStatus.NEW -> return next == GlobalTransitTimeSegmentStatus.FAILED
            GlobalTransitTimeSegmentStatus.QUEUED -> return next == GlobalTransitTimeSegmentStatus.DONE || next == GlobalTransitTimeSegmentStatus.FAILED
            GlobalTransitTimeSegmentStatus.RUNNING -> {
                when (next) {
                    GlobalTransitTimeSegmentStatus.FAILED -> return true
                }
                return false
            }
            GlobalTransitTimeSegmentStatus.DONE -> {
                when (next) {
                    GlobalTransitTimeSegmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalTransitTimeSegmentStatus) {
        if (!canMoveTo(next)) {
            error("global transit time segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedValue(): Double = round((threshold - rate) * 100) / 100

    fun snapshot(): GlobalTransitTimeSegment {
        return GlobalTransitTimeSegment(
            code = code,
            label = label,
            threshold = threshold,
            retries = retries,
            archived = archived,
            source = source,
            vehicleId = vehicleId,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global transit time segment in status new. */
fun newGlobalTransitTimeSegment(code: String, label: String): GlobalTransitTimeSegment {
    return GlobalTransitTimeSegment(
        code = code,
        label = label,
        threshold = 0.0,
        retries = 0,
        archived = false,
        source = "",
        vehicleId = "",
        rate = 0.0,
        status = GlobalTransitTimeSegmentStatus.NEW,
    )
}
