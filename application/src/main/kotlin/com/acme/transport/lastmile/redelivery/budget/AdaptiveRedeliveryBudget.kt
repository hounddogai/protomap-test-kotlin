package com.acme.transport.lastmile.redelivery.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive redelivery budget records of lastmile's redelivery work, and the rules, stores, and
// calculations around them.

/** Where a adaptive redelivery budget is in its life cycle. */
enum class AdaptiveRedeliveryBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive redelivery budget as the redelivery team keeps it. */
data class AdaptiveRedeliveryBudget(
    var code: String,
    var label: String,
    var attempts: Int,
    var floorValue: Double,
    var ratio: Double,
    var status: AdaptiveRedeliveryBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRedeliveryBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRedeliveryBudgetStatus): Boolean {
        when (status) {
            AdaptiveRedeliveryBudgetStatus.IDLE -> {
                when (next) {
                    AdaptiveRedeliveryBudgetStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveRedeliveryBudgetStatus.WARMING -> return next == AdaptiveRedeliveryBudgetStatus.DRAINING || next == AdaptiveRedeliveryBudgetStatus.STOPPED
            AdaptiveRedeliveryBudgetStatus.READY -> {
                when (next) {
                    AdaptiveRedeliveryBudgetStatus.DRAINING, AdaptiveRedeliveryBudgetStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveRedeliveryBudgetStatus.DRAINING -> return next == AdaptiveRedeliveryBudgetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AdaptiveRedeliveryBudgetStatus) {
        if (!canMoveTo(next)) {
            error("adaptive redelivery budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive redelivery budget"} $code"

    fun adjustedExposure(): Double = round((floorValue - attempts.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveRedeliveryBudget {
        return AdaptiveRedeliveryBudget(
            code = code,
            label = label,
            attempts = attempts,
            floorValue = floorValue,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive redelivery budget in status idle. */
fun newAdaptiveRedeliveryBudget(code: String, label: String): AdaptiveRedeliveryBudget {
    return AdaptiveRedeliveryBudget(
        code = code,
        label = label,
        attempts = 0,
        floorValue = 0.0,
        ratio = 0.0,
        status = AdaptiveRedeliveryBudgetStatus.IDLE,
    )
}
