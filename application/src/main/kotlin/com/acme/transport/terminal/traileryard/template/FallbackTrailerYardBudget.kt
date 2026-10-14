package com.acme.transport.terminal.traileryard.template

import com.acme.transport.kit.text.normalize

/** Where a fallback trailer yard budget is in its life cycle. */
enum class FallbackTrailerYardBudgetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A fallback trailer yard budget as the trailer yard team keeps it. */
data class FallbackTrailerYardBudget(
    var code: String,
    var name: String,
    var odometer: Int,
    var currency: String,
    var attributes: MutableMap<String, String>,
    var status: FallbackTrailerYardBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTrailerYardBudgetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTrailerYardBudgetStatus): Boolean {
        when (status) {
            FallbackTrailerYardBudgetStatus.PLANNED -> return next == FallbackTrailerYardBudgetStatus.COMPLETED
            FallbackTrailerYardBudgetStatus.SCHEDULED -> return next == FallbackTrailerYardBudgetStatus.COMPLETED || next == FallbackTrailerYardBudgetStatus.CANCELLED
            FallbackTrailerYardBudgetStatus.IN_PROGRESS -> {
                when (next) {
                    FallbackTrailerYardBudgetStatus.COMPLETED, FallbackTrailerYardBudgetStatus.CANCELLED -> return true
                }
                return false
            }
            FallbackTrailerYardBudgetStatus.COMPLETED -> {
                when (next) {
                    FallbackTrailerYardBudgetStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackTrailerYardBudgetStatus) {
        if (!canMoveTo(next)) {
            error("fallback trailer yard budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): FallbackTrailerYardBudget {
        return FallbackTrailerYardBudget(
            code = code,
            name = name,
            odometer = odometer,
            currency = currency,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback trailer yard budget in status planned. */
fun newFallbackTrailerYardBudget(code: String, name: String): FallbackTrailerYardBudget {
    return FallbackTrailerYardBudget(
        code = code,
        name = name,
        odometer = 0,
        currency = "",
        attributes = mutableMapOf<String, String>(),
        status = FallbackTrailerYardBudgetStatus.PLANNED,
    )
}
