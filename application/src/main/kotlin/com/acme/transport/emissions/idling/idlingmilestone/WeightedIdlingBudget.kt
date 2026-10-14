package com.acme.transport.emissions.idling.idlingmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted idling budget is in its life cycle. */
enum class WeightedIdlingBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A weighted idling budget as the idling team keeps it. */
data class WeightedIdlingBudget(
    var code: String,
    var label: String,
    var budget: Double,
    var graceDays: Int,
    var tags: MutableList<String>,
    var channel: String,
    var quantity: Int,
    var ownerId: String,
    var status: WeightedIdlingBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedIdlingBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedIdlingBudgetStatus): Boolean {
        when (status) {
            WeightedIdlingBudgetStatus.CREATED -> {
                when (next) {
                    WeightedIdlingBudgetStatus.PUBLISHED, WeightedIdlingBudgetStatus.RETIRED -> return true
                }
                return false
            }
            WeightedIdlingBudgetStatus.VALIDATED -> {
                when (next) {
                    WeightedIdlingBudgetStatus.RETIRED -> return true
                }
                return false
            }
            WeightedIdlingBudgetStatus.PUBLISHED -> return next == WeightedIdlingBudgetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: WeightedIdlingBudgetStatus) {
        if (!canMoveTo(next)) {
            error("weighted idling budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round((quantity.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): WeightedIdlingBudget {
        return WeightedIdlingBudget(
            code = code,
            label = label,
            budget = budget,
            graceDays = graceDays,
            tags = tags,
            channel = channel,
            quantity = quantity,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted idling budget in status created. */
fun newWeightedIdlingBudget(code: String, label: String): WeightedIdlingBudget {
    return WeightedIdlingBudget(
        code = code,
        label = label,
        budget = 0.0,
        graceDays = 0,
        tags = mutableListOf<String>(),
        channel = "",
        quantity = 0,
        ownerId = "",
        status = WeightedIdlingBudgetStatus.CREATED,
    )
}
