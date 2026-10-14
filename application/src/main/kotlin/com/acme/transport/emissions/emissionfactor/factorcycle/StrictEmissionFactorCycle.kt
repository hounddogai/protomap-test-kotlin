package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.kit.text.normalize

// The strict emission factor cycle records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a strict emission factor cycle is in its life cycle. */
enum class StrictEmissionFactorCycleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a strict emission factor cycle and its current status. */
data class StrictEmissionFactorCycle(
    var code: String,
    var title: String,
    var visible: Boolean,
    var contactEmail: String,
    var depot: String,
    var amount: Double,
    var status: StrictEmissionFactorCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictEmissionFactorCycleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictEmissionFactorCycleStatus): Boolean {
        when (status) {
            StrictEmissionFactorCycleStatus.OPEN -> {
                when (next) {
                    StrictEmissionFactorCycleStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictEmissionFactorCycleStatus.IN_REVIEW -> return next == StrictEmissionFactorCycleStatus.ARCHIVED
            StrictEmissionFactorCycleStatus.RESOLVED -> return next == StrictEmissionFactorCycleStatus.ARCHIVED
            StrictEmissionFactorCycleStatus.REOPENED -> return next == StrictEmissionFactorCycleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictEmissionFactorCycleStatus) {
        if (!canMoveTo(next)) {
            error("strict emission factor cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = amount

    fun snapshot(): StrictEmissionFactorCycle {
        return StrictEmissionFactorCycle(
            code = code,
            title = title,
            visible = visible,
            contactEmail = contactEmail,
            depot = depot,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict emission factor cycle in status open. */
fun newStrictEmissionFactorCycle(code: String, title: String): StrictEmissionFactorCycle {
    return StrictEmissionFactorCycle(
        code = code,
        title = title,
        visible = false,
        contactEmail = "",
        depot = "",
        amount = 0.0,
        status = StrictEmissionFactorCycleStatus.OPEN,
    )
}
