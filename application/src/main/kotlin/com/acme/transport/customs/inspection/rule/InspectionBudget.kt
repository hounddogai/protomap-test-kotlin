package com.acme.transport.customs.inspection.rule

import com.acme.transport.kit.text.normalize

/** Where a inspection budget is in its life cycle. */
enum class InspectionBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a inspection budget and its current status. */
data class InspectionBudget(
    var code: String,
    var label: String,
    var name: String,
    var routeCode: String,
    var category: String,
    var channel: String,
    var amount: Double,
    var summary: String,
    var status: InspectionBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionBudgetStatus): Boolean {
        when (status) {
            InspectionBudgetStatus.REQUESTED -> {
                when (next) {
                    InspectionBudgetStatus.REVOKED, InspectionBudgetStatus.EXPIRED -> return true
                }
                return false
            }
            InspectionBudgetStatus.GRANTED -> return next == InspectionBudgetStatus.REVOKED || next == InspectionBudgetStatus.EXPIRED
            InspectionBudgetStatus.REVOKED -> return next == InspectionBudgetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: InspectionBudgetStatus) {
        if (!canMoveTo(next)) {
            error("inspection budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = amount

    fun snapshot(): InspectionBudget {
        return InspectionBudget(
            code = code,
            label = label,
            name = name,
            routeCode = routeCode,
            category = category,
            channel = channel,
            amount = amount,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a inspection budget in status requested. */
fun newInspectionBudget(code: String, label: String): InspectionBudget {
    return InspectionBudget(
        code = code,
        label = label,
        name = "",
        routeCode = "",
        category = "",
        channel = "",
        amount = 0.0,
        summary = "",
        status = InspectionBudgetStatus.REQUESTED,
    )
}
