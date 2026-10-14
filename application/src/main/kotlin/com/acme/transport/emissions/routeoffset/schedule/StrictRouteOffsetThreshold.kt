package com.acme.transport.emissions.routeoffset.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict route offset threshold is in its life cycle. */
enum class StrictRouteOffsetThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One strict route offset threshold, with what the planners need to act on it. */
data class StrictRouteOffsetThreshold(
    var code: String,
    var title: String,
    var endsAt: Int,
    var createdAt: Int,
    var windowDays: Int,
    var status: StrictRouteOffsetThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRouteOffsetThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRouteOffsetThresholdStatus): Boolean {
        when (status) {
            StrictRouteOffsetThresholdStatus.REQUESTED -> return next == StrictRouteOffsetThresholdStatus.GRANTED
            StrictRouteOffsetThresholdStatus.GRANTED -> return next == StrictRouteOffsetThresholdStatus.REVOKED
            StrictRouteOffsetThresholdStatus.REVOKED -> {
                when (next) {
                    StrictRouteOffsetThresholdStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictRouteOffsetThresholdStatus) {
        if (!canMoveTo(next)) {
            error("strict route offset threshold $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveTotal(): Double = round(windowDays.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): StrictRouteOffsetThreshold {
        return StrictRouteOffsetThreshold(
            code = code,
            title = title,
            endsAt = endsAt,
            createdAt = createdAt,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict route offset threshold in status requested. */
fun newStrictRouteOffsetThreshold(code: String, title: String): StrictRouteOffsetThreshold {
    return StrictRouteOffsetThreshold(
        code = code,
        title = title,
        endsAt = 0,
        createdAt = 0,
        windowDays = 0,
        status = StrictRouteOffsetThresholdStatus.REQUESTED,
    )
}
