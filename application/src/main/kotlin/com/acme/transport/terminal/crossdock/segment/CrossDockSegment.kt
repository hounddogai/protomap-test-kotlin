package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.text.normalize

// The cross dock segment records of terminal's cross dock work, and the rules, stores, and calculations
// around them.

/** Where a cross dock segment is in its life cycle. */
enum class CrossDockSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A cross dock segment. Its code is unique within a tenant. */
data class CrossDockSegment(
    var code: String,
    var label: String,
    var routeCode: String,
    var fuelLiters: Double,
    var description: String,
    var status: CrossDockSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockSegmentStatus): Boolean {
        when (status) {
            CrossDockSegmentStatus.OPEN -> {
                when (next) {
                    CrossDockSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            CrossDockSegmentStatus.IN_REVIEW -> {
                when (next) {
                    CrossDockSegmentStatus.RESOLVED, CrossDockSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            CrossDockSegmentStatus.RESOLVED -> return next == CrossDockSegmentStatus.REOPENED
            CrossDockSegmentStatus.REOPENED -> return next == CrossDockSegmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CrossDockSegmentStatus) {
        if (!canMoveTo(next)) {
            error("cross dock segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): CrossDockSegment {
        return CrossDockSegment(
            code = code,
            label = label,
            routeCode = routeCode,
            fuelLiters = fuelLiters,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a cross dock segment in status open. */
fun newCrossDockSegment(code: String, label: String): CrossDockSegment {
    return CrossDockSegment(
        code = code,
        label = label,
        routeCode = "",
        fuelLiters = 0.0,
        description = "",
        status = CrossDockSegmentStatus.OPEN,
    )
}
