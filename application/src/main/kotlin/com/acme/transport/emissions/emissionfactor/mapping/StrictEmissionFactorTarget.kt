package com.acme.transport.emissions.emissionfactor.mapping

import com.acme.transport.kit.text.normalize

/** Where a strict emission factor target is in its life cycle. */
enum class StrictEmissionFactorTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A strict emission factor target. Its code is unique within a tenant. */
data class StrictEmissionFactorTarget(
    var code: String,
    var name: String,
    var graceDays: Int,
    var trailerId: String,
    var segment: String,
    var pinned: Boolean,
    var status: StrictEmissionFactorTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictEmissionFactorTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictEmissionFactorTargetStatus): Boolean {
        when (status) {
            StrictEmissionFactorTargetStatus.PLANNED -> return next == StrictEmissionFactorTargetStatus.COMPLETED
            StrictEmissionFactorTargetStatus.SCHEDULED -> return next == StrictEmissionFactorTargetStatus.COMPLETED || next == StrictEmissionFactorTargetStatus.CANCELLED
            StrictEmissionFactorTargetStatus.IN_PROGRESS -> {
                when (next) {
                    StrictEmissionFactorTargetStatus.CANCELLED -> return true
                }
                return false
            }
            StrictEmissionFactorTargetStatus.COMPLETED -> return next == StrictEmissionFactorTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: StrictEmissionFactorTargetStatus) {
        if (!canMoveTo(next)) {
            error("strict emission factor target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict emission factor target"} $code"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): StrictEmissionFactorTarget {
        return StrictEmissionFactorTarget(
            code = code,
            name = name,
            graceDays = graceDays,
            trailerId = trailerId,
            segment = segment,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a strict emission factor target in status planned. */
fun newStrictEmissionFactorTarget(code: String, name: String): StrictEmissionFactorTarget {
    return StrictEmissionFactorTarget(
        code = code,
        name = name,
        graceDays = 0,
        trailerId = "",
        segment = "",
        pinned = false,
        status = StrictEmissionFactorTargetStatus.PLANNED,
    )
}
