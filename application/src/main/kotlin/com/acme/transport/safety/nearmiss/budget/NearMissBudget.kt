package com.acme.transport.safety.nearmiss.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The near miss budget records of safety's near miss work, and the rules, stores, and calculations around
// them.

/** Where a near miss budget is in its life cycle. */
enum class NearMissBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a near miss budget and its current status. */
data class NearMissBudget(
    var code: String,
    var name: String,
    var contactEmail: String,
    var amount: Double,
    var shiftCode: String,
    var budget: Double,
    var dock: String,
    var status: NearMissBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissBudgetStatus): Boolean {
        when (status) {
            NearMissBudgetStatus.IDLE -> return next == NearMissBudgetStatus.READY
            NearMissBudgetStatus.WARMING -> return next == NearMissBudgetStatus.DRAINING
            NearMissBudgetStatus.READY -> return next == NearMissBudgetStatus.STOPPED
            NearMissBudgetStatus.DRAINING -> {
                when (next) {
                    NearMissBudgetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissBudgetStatus) {
        if (!canMoveTo(next)) {
            error("near miss budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedVolume(): Double = round((budget - amount) * 100) / 100

    fun snapshot(): NearMissBudget {
        return NearMissBudget(
            code = code,
            name = name,
            contactEmail = contactEmail,
            amount = amount,
            shiftCode = shiftCode,
            budget = budget,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a near miss budget in status idle. */
fun newNearMissBudget(code: String, name: String): NearMissBudget {
    return NearMissBudget(
        code = code,
        name = name,
        contactEmail = "",
        amount = 0.0,
        shiftCode = "",
        budget = 0.0,
        dock = "",
        status = NearMissBudgetStatus.IDLE,
    )
}
