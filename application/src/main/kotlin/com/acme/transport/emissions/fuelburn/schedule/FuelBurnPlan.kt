package com.acme.transport.emissions.fuelburn.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn plan is in its life cycle. */
enum class FuelBurnPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One fuel burn plan, with what the planners need to act on it. */
data class FuelBurnPlan(
    var code: String,
    var title: String,
    var retries: Int,
    var etaMinutes: Int,
    var locale: String,
    var reviewer: String,
    var attributes: MutableMap<String, String>,
    var status: FuelBurnPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnPlanStatus): Boolean {
        when (status) {
            FuelBurnPlanStatus.OPEN -> return next == FuelBurnPlanStatus.IN_REVIEW || next == FuelBurnPlanStatus.REOPENED
            FuelBurnPlanStatus.IN_REVIEW -> return next == FuelBurnPlanStatus.ARCHIVED
            FuelBurnPlanStatus.RESOLVED -> {
                when (next) {
                    FuelBurnPlanStatus.ARCHIVED -> return true
                }
                return false
            }
            FuelBurnPlanStatus.REOPENED -> return next == FuelBurnPlanStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FuelBurnPlanStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fuel burn plan"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round((etaMinutes.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnPlan {
        return FuelBurnPlan(
            code = code,
            title = title,
            retries = retries,
            etaMinutes = etaMinutes,
            locale = locale,
            reviewer = reviewer,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fuel burn plan in status open. */
fun newFuelBurnPlan(code: String, title: String): FuelBurnPlan {
    return FuelBurnPlan(
        code = code,
        title = title,
        retries = 0,
        etaMinutes = 0,
        locale = "",
        reviewer = "",
        attributes = mutableMapOf<String, String>(),
        status = FuelBurnPlanStatus.OPEN,
    )
}
