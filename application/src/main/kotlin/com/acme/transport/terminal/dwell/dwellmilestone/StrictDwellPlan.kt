package com.acme.transport.terminal.dwell.dwellmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict dwell plan is in its life cycle. */
enum class StrictDwellPlanStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a strict dwell plan and its current status. */
data class StrictDwellPlan(
    var code: String,
    var label: String,
    var limit: Int,
    var approved: Boolean,
    var minimum: Double,
    var graceDays: Int,
    var version: Int,
    var quantity: Int,
    var durationMinutes: Int,
    var reviewer: String,
    var status: StrictDwellPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDwellPlanStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDwellPlanStatus): Boolean {
        when (status) {
            StrictDwellPlanStatus.PROPOSED -> {
                when (next) {
                    StrictDwellPlanStatus.REJECTED -> return true
                }
                return false
            }
            StrictDwellPlanStatus.APPROVED -> {
                when (next) {
                    StrictDwellPlanStatus.REJECTED, StrictDwellPlanStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictDwellPlanStatus.REJECTED -> return next == StrictDwellPlanStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: StrictDwellPlanStatus) {
        if (!canMoveTo(next)) {
            error("strict dwell plan $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedValue(): Double = round((minimum - version.toDouble()) * 100) / 100

    fun snapshot(): StrictDwellPlan {
        return StrictDwellPlan(
            code = code,
            label = label,
            limit = limit,
            approved = approved,
            minimum = minimum,
            graceDays = graceDays,
            version = version,
            quantity = quantity,
            durationMinutes = durationMinutes,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict dwell plan in status proposed. */
fun newStrictDwellPlan(code: String, label: String): StrictDwellPlan {
    return StrictDwellPlan(
        code = code,
        label = label,
        limit = 0,
        approved = false,
        minimum = 0.0,
        graceDays = 0,
        version = 0,
        quantity = 0,
        durationMinutes = 0,
        reviewer = "",
        status = StrictDwellPlanStatus.PROPOSED,
    )
}
