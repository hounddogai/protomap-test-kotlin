package com.acme.transport.customs.broker.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The broker budget records of customs's broker work, and the rules, stores, and calculations around them.

/** Where a broker budget is in its life cycle. */
enum class BrokerBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One broker budget, with what the planners need to act on it. */
data class BrokerBudget(
    var code: String,
    var title: String,
    var notes: String,
    var vehicleId: String,
    var margin: Double,
    var sequence: Int,
    var status: BrokerBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerBudgetStatus): Boolean {
        when (status) {
            BrokerBudgetStatus.OPEN -> return next == BrokerBudgetStatus.IN_REVIEW || next == BrokerBudgetStatus.ARCHIVED
            BrokerBudgetStatus.IN_REVIEW -> {
                when (next) {
                    BrokerBudgetStatus.RESOLVED, BrokerBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
            BrokerBudgetStatus.RESOLVED -> return next == BrokerBudgetStatus.REOPENED || next == BrokerBudgetStatus.ARCHIVED
            BrokerBudgetStatus.REOPENED -> {
                when (next) {
                    BrokerBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BrokerBudgetStatus) {
        if (!canMoveTo(next)) {
            error("broker budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"broker budget"} $code"

    fun projectedTotal(): Double = round(sequence.toDouble() * margin * 100) / 100

    fun snapshot(): BrokerBudget {
        return BrokerBudget(
            code = code,
            title = title,
            notes = notes,
            vehicleId = vehicleId,
            margin = margin,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a broker budget in status open. */
fun newBrokerBudget(code: String, title: String): BrokerBudget {
    return BrokerBudget(
        code = code,
        title = title,
        notes = "",
        vehicleId = "",
        margin = 0.0,
        sequence = 0,
        status = BrokerBudgetStatus.OPEN,
    )
}
