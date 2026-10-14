package com.acme.transport.fleetops.defect.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal defect budget records of fleetops's defect work, and the rules, stores, and calculations
// around them.

/** Where a seasonal defect budget is in its life cycle. */
enum class SeasonalDefectBudgetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A seasonal defect budget as the defect team keeps it. */
data class SeasonalDefectBudget(
    var code: String,
    var title: String,
    var vehicleId: String,
    var timezone: String,
    var ratio: Double,
    var retries: Int,
    var ceiling: Double,
    var windowDays: Int,
    var updatedAt: Int,
    var status: SeasonalDefectBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDefectBudgetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDefectBudgetStatus): Boolean {
        when (status) {
            SeasonalDefectBudgetStatus.PLANNED -> {
                when (next) {
                    SeasonalDefectBudgetStatus.IN_PROGRESS -> return true
                }
                return false
            }
            SeasonalDefectBudgetStatus.SCHEDULED -> {
                when (next) {
                    SeasonalDefectBudgetStatus.IN_PROGRESS, SeasonalDefectBudgetStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalDefectBudgetStatus.IN_PROGRESS -> return next == SeasonalDefectBudgetStatus.COMPLETED
            SeasonalDefectBudgetStatus.COMPLETED -> return next == SeasonalDefectBudgetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalDefectBudgetStatus) {
        if (!canMoveTo(next)) {
            error("seasonal defect budget $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedValue(): Double = round((updatedAt.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): SeasonalDefectBudget {
        return SeasonalDefectBudget(
            code = code,
            title = title,
            vehicleId = vehicleId,
            timezone = timezone,
            ratio = ratio,
            retries = retries,
            ceiling = ceiling,
            windowDays = windowDays,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal defect budget in status planned. */
fun newSeasonalDefectBudget(code: String, title: String): SeasonalDefectBudget {
    return SeasonalDefectBudget(
        code = code,
        title = title,
        vehicleId = "",
        timezone = "",
        ratio = 0.0,
        retries = 0,
        ceiling = 0.0,
        windowDays = 0,
        updatedAt = 0,
        status = SeasonalDefectBudgetStatus.PLANNED,
    )
}
