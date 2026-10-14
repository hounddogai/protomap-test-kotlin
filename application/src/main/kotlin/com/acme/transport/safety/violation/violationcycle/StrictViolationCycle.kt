package com.acme.transport.safety.violation.violationcycle

import com.acme.transport.kit.text.normalize

/** Where a strict violation cycle is in its life cycle. */
enum class StrictViolationCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a strict violation cycle and its current status. */
data class StrictViolationCycle(
    var code: String,
    var name: String,
    var ownerId: String,
    var budget: Double,
    var locale: String,
    var status: StrictViolationCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictViolationCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictViolationCycleStatus): Boolean {
        when (status) {
            StrictViolationCycleStatus.PROPOSED -> {
                when (next) {
                    StrictViolationCycleStatus.REJECTED -> return true
                }
                return false
            }
            StrictViolationCycleStatus.APPROVED -> {
                when (next) {
                    StrictViolationCycleStatus.REJECTED -> return true
                }
                return false
            }
            StrictViolationCycleStatus.REJECTED -> return next == StrictViolationCycleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: StrictViolationCycleStatus) {
        if (!canMoveTo(next)) {
            error("strict violation cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = budget

    fun snapshot(): StrictViolationCycle {
        return StrictViolationCycle(
            code = code,
            name = name,
            ownerId = ownerId,
            budget = budget,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a strict violation cycle in status proposed. */
fun newStrictViolationCycle(code: String, name: String): StrictViolationCycle {
    return StrictViolationCycle(
        code = code,
        name = name,
        ownerId = "",
        budget = 0.0,
        locale = "",
        status = StrictViolationCycleStatus.PROPOSED,
    )
}
