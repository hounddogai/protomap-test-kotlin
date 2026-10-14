package com.acme.transport.customs.clearance.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance budget is in its life cycle. */
enum class ClearanceBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a clearance budget and its current status. */
data class ClearanceBudget(
    var code: String,
    var name: String,
    var floorValue: Double,
    var depot: String,
    var source: String,
    var threshold: Double,
    var title: String,
    var locale: String,
    var sequence: Int,
    var shiftCode: String,
    var budget: Double,
    var status: ClearanceBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceBudgetStatus): Boolean {
        when (status) {
            ClearanceBudgetStatus.OPEN -> {
                when (next) {
                    ClearanceBudgetStatus.RESOLVED, ClearanceBudgetStatus.REOPENED -> return true
                }
                return false
            }
            ClearanceBudgetStatus.IN_REVIEW -> return next == ClearanceBudgetStatus.REOPENED || next == ClearanceBudgetStatus.ARCHIVED
            ClearanceBudgetStatus.RESOLVED -> return next == ClearanceBudgetStatus.REOPENED || next == ClearanceBudgetStatus.ARCHIVED
            ClearanceBudgetStatus.REOPENED -> return next == ClearanceBudgetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ClearanceBudgetStatus) {
        if (!canMoveTo(next)) {
            error("clearance budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"clearance budget"} $code"

    fun projectedExposure(): Double = round((sequence.toDouble() + budget) * 100) / 100

    fun snapshot(): ClearanceBudget {
        return ClearanceBudget(
            code = code,
            name = name,
            floorValue = floorValue,
            depot = depot,
            source = source,
            threshold = threshold,
            title = title,
            locale = locale,
            sequence = sequence,
            shiftCode = shiftCode,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a clearance budget in status open. */
fun newClearanceBudget(code: String, name: String): ClearanceBudget {
    return ClearanceBudget(
        code = code,
        name = name,
        floorValue = 0.0,
        depot = "",
        source = "",
        threshold = 0.0,
        title = "",
        locale = "",
        sequence = 0,
        shiftCode = "",
        budget = 0.0,
        status = ClearanceBudgetStatus.OPEN,
    )
}
