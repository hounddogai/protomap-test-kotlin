package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal violation allocation records of safety's violation work, and the rules, stores, and
// calculations around them.

/** Where a seasonal violation allocation is in its life cycle. */
enum class SeasonalViolationAllocationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal violation allocation. Its code is unique within a tenant. */
data class SeasonalViolationAllocation(
    var code: String,
    var label: String,
    var phone: String,
    var durationMinutes: Int,
    var maximum: Double,
    var status: SeasonalViolationAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalViolationAllocationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalViolationAllocationStatus): Boolean {
        when (status) {
            SeasonalViolationAllocationStatus.OPEN -> return next == SeasonalViolationAllocationStatus.IN_REVIEW || next == SeasonalViolationAllocationStatus.REOPENED
            SeasonalViolationAllocationStatus.IN_REVIEW -> return next == SeasonalViolationAllocationStatus.REOPENED
            SeasonalViolationAllocationStatus.RESOLVED -> return next == SeasonalViolationAllocationStatus.REOPENED
            SeasonalViolationAllocationStatus.REOPENED -> {
                when (next) {
                    SeasonalViolationAllocationStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalViolationAllocationStatus) {
        if (!canMoveTo(next)) {
            error("seasonal violation allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal violation allocation"} $code"

    fun projectedVolume(): Double = round((maximum - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): SeasonalViolationAllocation {
        return SeasonalViolationAllocation(
            code = code,
            label = label,
            phone = phone,
            durationMinutes = durationMinutes,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal violation allocation in status open. */
fun newSeasonalViolationAllocation(code: String, label: String): SeasonalViolationAllocation {
    return SeasonalViolationAllocation(
        code = code,
        label = label,
        phone = "",
        durationMinutes = 0,
        maximum = 0.0,
        status = SeasonalViolationAllocationStatus.OPEN,
    )
}
