package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback coaching budget is in its life cycle. */
enum class FallbackCoachingBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a fallback coaching budget and its current status. */
data class FallbackCoachingBudget(
    var code: String,
    var label: String,
    var minimum: Double,
    var ceiling: Double,
    var amount: Double,
    var pallets: Int,
    var attributes: MutableMap<String, String>,
    var visible: Boolean,
    var status: FallbackCoachingBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCoachingBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCoachingBudgetStatus): Boolean {
        when (status) {
            FallbackCoachingBudgetStatus.IDLE -> {
                when (next) {
                    FallbackCoachingBudgetStatus.READY, FallbackCoachingBudgetStatus.DRAINING -> return true
                }
                return false
            }
            FallbackCoachingBudgetStatus.WARMING -> return next == FallbackCoachingBudgetStatus.STOPPED
            FallbackCoachingBudgetStatus.READY -> {
                when (next) {
                    FallbackCoachingBudgetStatus.STOPPED -> return true
                }
                return false
            }
            FallbackCoachingBudgetStatus.DRAINING -> {
                when (next) {
                    FallbackCoachingBudgetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackCoachingBudgetStatus) {
        if (!canMoveTo(next)) {
            error("fallback coaching budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fallback coaching budget"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round((pallets.toDouble() + ceiling) * 100) / 100

    fun snapshot(): FallbackCoachingBudget {
        return FallbackCoachingBudget(
            code = code,
            label = label,
            minimum = minimum,
            ceiling = ceiling,
            amount = amount,
            pallets = pallets,
            attributes = attributes,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback coaching budget in status idle. */
fun newFallbackCoachingBudget(code: String, label: String): FallbackCoachingBudget {
    return FallbackCoachingBudget(
        code = code,
        label = label,
        minimum = 0.0,
        ceiling = 0.0,
        amount = 0.0,
        pallets = 0,
        attributes = mutableMapOf<String, String>(),
        visible = false,
        status = FallbackCoachingBudgetStatus.IDLE,
    )
}
