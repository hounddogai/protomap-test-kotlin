package com.acme.transport.freightpay.ratecard.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rate card cycle records of freightpay's rate card work, and the rules, stores, and calculations around
// them.

/** Where a rate card cycle is in its life cycle. */
enum class RateCardCycleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One rate card cycle, with what the planners need to act on it. */
data class RateCardCycle(
    var code: String,
    var label: String,
    var attempts: Int,
    var cost: Double,
    var name: String,
    var maximum: Double,
    var vehicleId: String,
    var status: RateCardCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardCycleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardCycleStatus): Boolean {
        when (status) {
            RateCardCycleStatus.OPEN -> {
                when (next) {
                    RateCardCycleStatus.REOPENED, RateCardCycleStatus.ARCHIVED -> return true
                }
                return false
            }
            RateCardCycleStatus.IN_REVIEW -> return next == RateCardCycleStatus.RESOLVED || next == RateCardCycleStatus.ARCHIVED
            RateCardCycleStatus.RESOLVED -> return next == RateCardCycleStatus.REOPENED || next == RateCardCycleStatus.ARCHIVED
            RateCardCycleStatus.REOPENED -> return next == RateCardCycleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RateCardCycleStatus) {
        if (!canMoveTo(next)) {
            error("rate card cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rate card cycle"} $code"

    fun projectedTotal(): Double = round((cost - attempts.toDouble()) * 100) / 100

    fun snapshot(): RateCardCycle {
        return RateCardCycle(
            code = code,
            label = label,
            attempts = attempts,
            cost = cost,
            name = name,
            maximum = maximum,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rate card cycle in status open. */
fun newRateCardCycle(code: String, label: String): RateCardCycle {
    return RateCardCycle(
        code = code,
        label = label,
        attempts = 0,
        cost = 0.0,
        name = "",
        maximum = 0.0,
        vehicleId = "",
        status = RateCardCycleStatus.OPEN,
    )
}
