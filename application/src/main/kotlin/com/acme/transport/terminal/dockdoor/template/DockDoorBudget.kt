package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door budget is in its life cycle. */
enum class DockDoorBudgetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A dock door budget. Its code is unique within a tenant. */
data class DockDoorBudget(
    var code: String,
    var title: String,
    var capacity: Int,
    var locale: String,
    var quantity: Int,
    var budget: Double,
    var shiftCode: String,
    var status: DockDoorBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorBudgetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorBudgetStatus): Boolean {
        when (status) {
            DockDoorBudgetStatus.PLANNED -> return next == DockDoorBudgetStatus.SCHEDULED
            DockDoorBudgetStatus.SCHEDULED -> {
                when (next) {
                    DockDoorBudgetStatus.COMPLETED -> return true
                }
                return false
            }
            DockDoorBudgetStatus.IN_PROGRESS -> return next == DockDoorBudgetStatus.CANCELLED
            DockDoorBudgetStatus.COMPLETED -> {
                when (next) {
                    DockDoorBudgetStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DockDoorBudgetStatus) {
        if (!canMoveTo(next)) {
            error("dock door budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"dock door budget"} $code"

    fun effectiveExposure(): Double = round(capacity.toDouble() * budget * 100) / 100

    fun snapshot(): DockDoorBudget {
        return DockDoorBudget(
            code = code,
            title = title,
            capacity = capacity,
            locale = locale,
            quantity = quantity,
            budget = budget,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a dock door budget in status planned. */
fun newDockDoorBudget(code: String, title: String): DockDoorBudget {
    return DockDoorBudget(
        code = code,
        title = title,
        capacity = 0,
        locale = "",
        quantity = 0,
        budget = 0.0,
        shiftCode = "",
        status = DockDoorBudgetStatus.PLANNED,
    )
}
