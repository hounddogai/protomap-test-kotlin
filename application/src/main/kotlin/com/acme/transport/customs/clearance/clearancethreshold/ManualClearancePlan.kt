package com.acme.transport.customs.clearance.clearancethreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual clearance plan is in its life cycle. */
enum class ManualClearancePlanStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One manual clearance plan, with what the planners need to act on it. */
data class ManualClearancePlan(
    var code: String,
    var name: String,
    var approved: Boolean,
    var durationMinutes: Int,
    var stopCount: Int,
    var status: ManualClearancePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualClearancePlanStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualClearancePlanStatus): Boolean {
        when (status) {
            ManualClearancePlanStatus.PROPOSED -> {
                when (next) {
                    ManualClearancePlanStatus.APPROVED, ManualClearancePlanStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualClearancePlanStatus.APPROVED -> return next == ManualClearancePlanStatus.REJECTED
            ManualClearancePlanStatus.REJECTED -> return next == ManualClearancePlanStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ManualClearancePlanStatus) {
        if (!canMoveTo(next)) {
            error("manual clearance plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedValue(): Double = round((durationMinutes.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): ManualClearancePlan {
        return ManualClearancePlan(
            code = code,
            name = name,
            approved = approved,
            durationMinutes = durationMinutes,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual clearance plan in status proposed. */
fun newManualClearancePlan(code: String, name: String): ManualClearancePlan {
    return ManualClearancePlan(
        code = code,
        name = name,
        approved = false,
        durationMinutes = 0,
        stopCount = 0,
        status = ManualClearancePlanStatus.PROPOSED,
    )
}
