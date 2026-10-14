package com.acme.transport.linehaul.trailer.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict trailer plan is in its life cycle. */
enum class StrictTrailerPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A strict trailer plan as the trailer team keeps it. */
data class StrictTrailerPlan(
    var code: String,
    var label: String,
    var limit: Int,
    var segment: String,
    var startsAt: Int,
    var driverId: String,
    var budget: Double,
    var checksum: String,
    var capacity: Int,
    var locale: String,
    var status: StrictTrailerPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTrailerPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTrailerPlanStatus): Boolean {
        when (status) {
            StrictTrailerPlanStatus.IDLE -> return next == StrictTrailerPlanStatus.READY || next == StrictTrailerPlanStatus.STOPPED
            StrictTrailerPlanStatus.WARMING -> return next == StrictTrailerPlanStatus.READY || next == StrictTrailerPlanStatus.DRAINING
            StrictTrailerPlanStatus.READY -> {
                when (next) {
                    StrictTrailerPlanStatus.DRAINING -> return true
                }
                return false
            }
            StrictTrailerPlanStatus.DRAINING -> return next == StrictTrailerPlanStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: StrictTrailerPlanStatus) {
        if (!canMoveTo(next)) {
            error("strict trailer plan $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveLoad(): Double = round((limit.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): StrictTrailerPlan {
        return StrictTrailerPlan(
            code = code,
            label = label,
            limit = limit,
            segment = segment,
            startsAt = startsAt,
            driverId = driverId,
            budget = budget,
            checksum = checksum,
            capacity = capacity,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a strict trailer plan in status idle. */
fun newStrictTrailerPlan(code: String, label: String): StrictTrailerPlan {
    return StrictTrailerPlan(
        code = code,
        label = label,
        limit = 0,
        segment = "",
        startsAt = 0,
        driverId = "",
        budget = 0.0,
        checksum = "",
        capacity = 0,
        locale = "",
        status = StrictTrailerPlanStatus.IDLE,
    )
}
