package com.acme.transport.safety.violation.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global violation budget is in its life cycle. */
enum class GlobalViolationBudgetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a global violation budget and its current status. */
data class GlobalViolationBudget(
    var code: String,
    var label: String,
    var graceDays: Int,
    var windowDays: Int,
    var startsAt: Int,
    var notes: String,
    var active: Boolean,
    var region: String,
    var status: GlobalViolationBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalViolationBudgetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalViolationBudgetStatus): Boolean {
        when (status) {
            GlobalViolationBudgetStatus.PROPOSED -> return next == GlobalViolationBudgetStatus.REJECTED || next == GlobalViolationBudgetStatus.WITHDRAWN
            GlobalViolationBudgetStatus.APPROVED -> return next == GlobalViolationBudgetStatus.REJECTED || next == GlobalViolationBudgetStatus.WITHDRAWN
            GlobalViolationBudgetStatus.REJECTED -> {
                when (next) {
                    GlobalViolationBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalViolationBudgetStatus) {
        if (!canMoveTo(next)) {
            error("global violation budget $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"global violation budget"} $code"

    fun weightedExposure(): Double = round(windowDays.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): GlobalViolationBudget {
        return GlobalViolationBudget(
            code = code,
            label = label,
            graceDays = graceDays,
            windowDays = windowDays,
            startsAt = startsAt,
            notes = notes,
            active = active,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a global violation budget in status proposed. */
fun newGlobalViolationBudget(code: String, label: String): GlobalViolationBudget {
    return GlobalViolationBudget(
        code = code,
        label = label,
        graceDays = 0,
        windowDays = 0,
        startsAt = 0,
        notes = "",
        active = false,
        region = "",
        status = GlobalViolationBudgetStatus.PROPOSED,
    )
}
