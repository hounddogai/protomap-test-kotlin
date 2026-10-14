package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.kit.text.normalize

// The strict dwell digest records of terminal's dwell work, and the rules, stores, and calculations around
// them.

/** Where a strict dwell digest is in its life cycle. */
enum class StrictDwellDigestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a strict dwell digest and its current status. */
data class StrictDwellDigest(
    var code: String,
    var title: String,
    var unit: String,
    var fuelLiters: Double,
    var active: Boolean,
    var status: StrictDwellDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDwellDigestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDwellDigestStatus): Boolean {
        when (status) {
            StrictDwellDigestStatus.PLANNED -> return next == StrictDwellDigestStatus.CANCELLED
            StrictDwellDigestStatus.SCHEDULED -> {
                when (next) {
                    StrictDwellDigestStatus.IN_PROGRESS, StrictDwellDigestStatus.COMPLETED -> return true
                }
                return false
            }
            StrictDwellDigestStatus.IN_PROGRESS -> {
                when (next) {
                    StrictDwellDigestStatus.COMPLETED, StrictDwellDigestStatus.CANCELLED -> return true
                }
                return false
            }
            StrictDwellDigestStatus.COMPLETED -> return next == StrictDwellDigestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: StrictDwellDigestStatus) {
        if (!canMoveTo(next)) {
            error("strict dwell digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict dwell digest"} $code"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): StrictDwellDigest {
        return StrictDwellDigest(
            code = code,
            title = title,
            unit = unit,
            fuelLiters = fuelLiters,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a strict dwell digest in status planned. */
fun newStrictDwellDigest(code: String, title: String): StrictDwellDigest {
    return StrictDwellDigest(
        code = code,
        title = title,
        unit = "",
        fuelLiters = 0.0,
        active = false,
        status = StrictDwellDigestStatus.PLANNED,
    )
}
