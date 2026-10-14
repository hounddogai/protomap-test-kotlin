package com.acme.transport.customs.inspection.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched inspection budget records of customs's inspection work, and the rules, stores, and calculations
// around them.

/** Where a batched inspection budget is in its life cycle. */
enum class BatchedInspectionBudgetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched inspection budget. Its code is unique within a tenant. */
data class BatchedInspectionBudget(
    var code: String,
    var title: String,
    var routeCode: String,
    var approved: Boolean,
    var unit: String,
    var category: String,
    var attempts: Int,
    var margin: Double,
    var name: String,
    var reviewer: String,
    var status: BatchedInspectionBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedInspectionBudgetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedInspectionBudgetStatus): Boolean {
        when (status) {
            BatchedInspectionBudgetStatus.PROPOSED -> return next == BatchedInspectionBudgetStatus.APPROVED
            BatchedInspectionBudgetStatus.APPROVED -> return next == BatchedInspectionBudgetStatus.REJECTED
            BatchedInspectionBudgetStatus.REJECTED -> {
                when (next) {
                    BatchedInspectionBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedInspectionBudgetStatus) {
        if (!canMoveTo(next)) {
            error("batched inspection budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveExposure(): Double = round((attempts.toDouble() - margin) * 100) / 100

    fun snapshot(): BatchedInspectionBudget {
        return BatchedInspectionBudget(
            code = code,
            title = title,
            routeCode = routeCode,
            approved = approved,
            unit = unit,
            category = category,
            attempts = attempts,
            margin = margin,
            name = name,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a batched inspection budget in status proposed. */
fun newBatchedInspectionBudget(code: String, title: String): BatchedInspectionBudget {
    return BatchedInspectionBudget(
        code = code,
        title = title,
        routeCode = "",
        approved = false,
        unit = "",
        category = "",
        attempts = 0,
        margin = 0.0,
        name = "",
        reviewer = "",
        status = BatchedInspectionBudgetStatus.PROPOSED,
    )
}
