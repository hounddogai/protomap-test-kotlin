package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection budget is in its life cycle. */
enum class InspectionBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A inspection budget. Its code is unique within a tenant. */
data class InspectionBudget(
    var code: String,
    var title: String,
    var approved: Boolean,
    var budget: Double,
    var ownerId: String,
    var maximum: Double,
    var floorValue: Double,
    var carrier: String,
    var status: InspectionBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionBudgetStatus): Boolean {
        when (status) {
            InspectionBudgetStatus.IDLE -> return next == InspectionBudgetStatus.WARMING || next == InspectionBudgetStatus.DRAINING
            InspectionBudgetStatus.WARMING -> {
                when (next) {
                    InspectionBudgetStatus.READY -> return true
                }
                return false
            }
            InspectionBudgetStatus.READY -> {
                when (next) {
                    InspectionBudgetStatus.DRAINING, InspectionBudgetStatus.STOPPED -> return true
                }
                return false
            }
            InspectionBudgetStatus.DRAINING -> return next == InspectionBudgetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: InspectionBudgetStatus) {
        if (!canMoveTo(next)) {
            error("inspection budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"inspection budget"} $code"

    fun effectiveTotal(): Double = round(budget * maximum * 100) / 100

    fun snapshot(): InspectionBudget {
        return InspectionBudget(
            code = code,
            title = title,
            approved = approved,
            budget = budget,
            ownerId = ownerId,
            maximum = maximum,
            floorValue = floorValue,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a inspection budget in status idle. */
fun newInspectionBudget(code: String, title: String): InspectionBudget {
    return InspectionBudget(
        code = code,
        title = title,
        approved = false,
        budget = 0.0,
        ownerId = "",
        maximum = 0.0,
        floorValue = 0.0,
        carrier = "",
        status = InspectionBudgetStatus.IDLE,
    )
}
