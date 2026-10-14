package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.kit.text.normalize

/** Where a strict route offset target is in its life cycle. */
enum class StrictRouteOffsetTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One strict route offset target, with what the planners need to act on it. */
data class StrictRouteOffsetTarget(
    var code: String,
    var label: String,
    var depot: String,
    var externalRef: String,
    var startsAt: Int,
    var trailerId: String,
    var status: StrictRouteOffsetTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRouteOffsetTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRouteOffsetTargetStatus): Boolean {
        when (status) {
            StrictRouteOffsetTargetStatus.PLANNED -> {
                when (next) {
                    StrictRouteOffsetTargetStatus.IN_PROGRESS, StrictRouteOffsetTargetStatus.COMPLETED -> return true
                }
                return false
            }
            StrictRouteOffsetTargetStatus.SCHEDULED -> return next == StrictRouteOffsetTargetStatus.COMPLETED || next == StrictRouteOffsetTargetStatus.CANCELLED
            StrictRouteOffsetTargetStatus.IN_PROGRESS -> return next == StrictRouteOffsetTargetStatus.COMPLETED || next == StrictRouteOffsetTargetStatus.CANCELLED
            StrictRouteOffsetTargetStatus.COMPLETED -> {
                when (next) {
                    StrictRouteOffsetTargetStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictRouteOffsetTargetStatus) {
        if (!canMoveTo(next)) {
            error("strict route offset target $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): StrictRouteOffsetTarget {
        return StrictRouteOffsetTarget(
            code = code,
            label = label,
            depot = depot,
            externalRef = externalRef,
            startsAt = startsAt,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict route offset target in status planned. */
fun newStrictRouteOffsetTarget(code: String, label: String): StrictRouteOffsetTarget {
    return StrictRouteOffsetTarget(
        code = code,
        label = label,
        depot = "",
        externalRef = "",
        startsAt = 0,
        trailerId = "",
        status = StrictRouteOffsetTargetStatus.PLANNED,
    )
}
