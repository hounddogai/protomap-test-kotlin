package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default near miss budget is in its life cycle. */
enum class DefaultNearMissBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One default near miss budget, with what the planners need to act on it. */
data class DefaultNearMissBudget(
    var code: String,
    var name: String,
    var vehicleId: String,
    var currency: String,
    var amount: Double,
    var priority: Int,
    var category: String,
    var updatedAt: Int,
    var status: DefaultNearMissBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultNearMissBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultNearMissBudgetStatus): Boolean {
        when (status) {
            DefaultNearMissBudgetStatus.IDLE -> return next == DefaultNearMissBudgetStatus.WARMING
            DefaultNearMissBudgetStatus.WARMING -> {
                when (next) {
                    DefaultNearMissBudgetStatus.DRAINING, DefaultNearMissBudgetStatus.STOPPED -> return true
                }
                return false
            }
            DefaultNearMissBudgetStatus.READY -> return next == DefaultNearMissBudgetStatus.STOPPED
            DefaultNearMissBudgetStatus.DRAINING -> {
                when (next) {
                    DefaultNearMissBudgetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultNearMissBudgetStatus) {
        if (!canMoveTo(next)) {
            error("default near miss budget $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default near miss budget"} $code"

    fun weightedExposure(): Double = round((priority.toDouble() - amount) * 100) / 100

    fun snapshot(): DefaultNearMissBudget {
        return DefaultNearMissBudget(
            code = code,
            name = name,
            vehicleId = vehicleId,
            currency = currency,
            amount = amount,
            priority = priority,
            category = category,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default near miss budget in status idle. */
fun newDefaultNearMissBudget(code: String, name: String): DefaultNearMissBudget {
    return DefaultNearMissBudget(
        code = code,
        name = name,
        vehicleId = "",
        currency = "",
        amount = 0.0,
        priority = 0,
        category = "",
        updatedAt = 0,
        status = DefaultNearMissBudgetStatus.IDLE,
    )
}
