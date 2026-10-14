package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict duty period allocation is in its life cycle. */
enum class StrictDutyPeriodAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A strict duty period allocation as the duty period team keeps it. */
data class StrictDutyPeriodAllocation(
    var code: String,
    var name: String,
    var depot: String,
    var trailerId: String,
    var score: Double,
    var category: String,
    var minimum: Double,
    var status: StrictDutyPeriodAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDutyPeriodAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDutyPeriodAllocationStatus): Boolean {
        when (status) {
            StrictDutyPeriodAllocationStatus.PLANNED -> {
                when (next) {
                    StrictDutyPeriodAllocationStatus.IN_PROGRESS -> return true
                }
                return false
            }
            StrictDutyPeriodAllocationStatus.SCHEDULED -> return next == StrictDutyPeriodAllocationStatus.CANCELLED
            StrictDutyPeriodAllocationStatus.IN_PROGRESS -> return next == StrictDutyPeriodAllocationStatus.COMPLETED || next == StrictDutyPeriodAllocationStatus.CANCELLED
            StrictDutyPeriodAllocationStatus.COMPLETED -> {
                when (next) {
                    StrictDutyPeriodAllocationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictDutyPeriodAllocationStatus) {
        if (!canMoveTo(next)) {
            error("strict duty period allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netValue(): Double = round((minimum - score) * 100) / 100

    fun snapshot(): StrictDutyPeriodAllocation {
        return StrictDutyPeriodAllocation(
            code = code,
            name = name,
            depot = depot,
            trailerId = trailerId,
            score = score,
            category = category,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict duty period allocation in status planned. */
fun newStrictDutyPeriodAllocation(code: String, name: String): StrictDutyPeriodAllocation {
    return StrictDutyPeriodAllocation(
        code = code,
        name = name,
        depot = "",
        trailerId = "",
        score = 0.0,
        category = "",
        minimum = 0.0,
        status = StrictDutyPeriodAllocationStatus.PLANNED,
    )
}
