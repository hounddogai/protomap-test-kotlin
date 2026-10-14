package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal service plan budget is in its life cycle. */
enum class SeasonalServicePlanBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal service plan budget as the service plan team keeps it. */
data class SeasonalServicePlanBudget(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var category: String,
    var shiftCode: String,
    var quantity: Int,
    var revision: Int,
    var label: String,
    var stopCount: Int,
    var status: SeasonalServicePlanBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalServicePlanBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalServicePlanBudgetStatus): Boolean {
        when (status) {
            SeasonalServicePlanBudgetStatus.REQUESTED -> {
                when (next) {
                    SeasonalServicePlanBudgetStatus.GRANTED, SeasonalServicePlanBudgetStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalServicePlanBudgetStatus.GRANTED -> {
                when (next) {
                    SeasonalServicePlanBudgetStatus.REVOKED -> return true
                }
                return false
            }
            SeasonalServicePlanBudgetStatus.REVOKED -> {
                when (next) {
                    SeasonalServicePlanBudgetStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalServicePlanBudgetStatus) {
        if (!canMoveTo(next)) {
            error("seasonal service plan budget $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((stopCount.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): SeasonalServicePlanBudget {
        return SeasonalServicePlanBudget(
            code = code,
            name = name,
            attributes = attributes,
            category = category,
            shiftCode = shiftCode,
            quantity = quantity,
            revision = revision,
            label = label,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a seasonal service plan budget in status requested. */
fun newSeasonalServicePlanBudget(code: String, name: String): SeasonalServicePlanBudget {
    return SeasonalServicePlanBudget(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        category = "",
        shiftCode = "",
        quantity = 0,
        revision = 0,
        label = "",
        stopCount = 0,
        status = SeasonalServicePlanBudgetStatus.REQUESTED,
    )
}
