package com.acme.transport.fleetops.utilization.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization budget is in its life cycle. */
enum class UtilizationBudgetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a utilization budget and its current status. */
data class UtilizationBudget(
    var code: String,
    var name: String,
    var category: String,
    var minimum: Double,
    var score: Double,
    var unit: String,
    var stopCount: Int,
    var etaMinutes: Int,
    var amount: Double,
    var pinned: Boolean,
    var checksum: String,
    var status: UtilizationBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationBudgetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationBudgetStatus): Boolean {
        when (status) {
            UtilizationBudgetStatus.PLANNED -> {
                when (next) {
                    UtilizationBudgetStatus.IN_PROGRESS -> return true
                }
                return false
            }
            UtilizationBudgetStatus.SCHEDULED -> {
                when (next) {
                    UtilizationBudgetStatus.COMPLETED -> return true
                }
                return false
            }
            UtilizationBudgetStatus.IN_PROGRESS -> return next == UtilizationBudgetStatus.COMPLETED || next == UtilizationBudgetStatus.CANCELLED
            UtilizationBudgetStatus.COMPLETED -> return next == UtilizationBudgetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: UtilizationBudgetStatus) {
        if (!canMoveTo(next)) {
            error("utilization budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"utilization budget"} $code"

    fun adjustedTotal(): Double = round((amount - stopCount.toDouble()) * 100) / 100

    fun snapshot(): UtilizationBudget {
        return UtilizationBudget(
            code = code,
            name = name,
            category = category,
            minimum = minimum,
            score = score,
            unit = unit,
            stopCount = stopCount,
            etaMinutes = etaMinutes,
            amount = amount,
            pinned = pinned,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a utilization budget in status planned. */
fun newUtilizationBudget(code: String, name: String): UtilizationBudget {
    return UtilizationBudget(
        code = code,
        name = name,
        category = "",
        minimum = 0.0,
        score = 0.0,
        unit = "",
        stopCount = 0,
        etaMinutes = 0,
        amount = 0.0,
        pinned = false,
        checksum = "",
        status = UtilizationBudgetStatus.PLANNED,
    )
}
