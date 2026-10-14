package com.acme.transport.customs.tariff.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted tariff budget is in its life cycle. */
enum class WeightedTariffBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One weighted tariff budget, with what the planners need to act on it. */
data class WeightedTariffBudget(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var driverId: String,
    var notes: String,
    var visible: Boolean,
    var budget: Double,
    var rate: Double,
    var region: String,
    var status: WeightedTariffBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTariffBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTariffBudgetStatus): Boolean {
        when (status) {
            WeightedTariffBudgetStatus.OPEN -> {
                when (next) {
                    WeightedTariffBudgetStatus.REOPENED -> return true
                }
                return false
            }
            WeightedTariffBudgetStatus.IN_REVIEW -> {
                when (next) {
                    WeightedTariffBudgetStatus.REOPENED, WeightedTariffBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedTariffBudgetStatus.RESOLVED -> {
                when (next) {
                    WeightedTariffBudgetStatus.REOPENED -> return true
                }
                return false
            }
            WeightedTariffBudgetStatus.REOPENED -> return next == WeightedTariffBudgetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: WeightedTariffBudgetStatus) {
        if (!canMoveTo(next)) {
            error("weighted tariff budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round(rate * budget * 100) / 100

    fun snapshot(): WeightedTariffBudget {
        return WeightedTariffBudget(
            code = code,
            title = title,
            tags = tags,
            driverId = driverId,
            notes = notes,
            visible = visible,
            budget = budget,
            rate = rate,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a weighted tariff budget in status open. */
fun newWeightedTariffBudget(code: String, title: String): WeightedTariffBudget {
    return WeightedTariffBudget(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        driverId = "",
        notes = "",
        visible = false,
        budget = 0.0,
        rate = 0.0,
        region = "",
        status = WeightedTariffBudgetStatus.OPEN,
    )
}
