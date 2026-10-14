package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global dwell budget is in its life cycle. */
enum class GlobalDwellBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One global dwell budget, with what the planners need to act on it. */
data class GlobalDwellBudget(
    var code: String,
    var label: String,
    var ceiling: Double,
    var quantity: Int,
    var loadKg: Double,
    var unit: String,
    var contactEmail: String,
    var graceDays: Int,
    var status: GlobalDwellBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDwellBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDwellBudgetStatus): Boolean {
        when (status) {
            GlobalDwellBudgetStatus.IDLE -> return next == GlobalDwellBudgetStatus.READY || next == GlobalDwellBudgetStatus.DRAINING
            GlobalDwellBudgetStatus.WARMING -> {
                when (next) {
                    GlobalDwellBudgetStatus.DRAINING -> return true
                }
                return false
            }
            GlobalDwellBudgetStatus.READY -> return next == GlobalDwellBudgetStatus.DRAINING
            GlobalDwellBudgetStatus.DRAINING -> return next == GlobalDwellBudgetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: GlobalDwellBudgetStatus) {
        if (!canMoveTo(next)) {
            error("global dwell budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netTotal(): Double = round(loadKg * quantity.toDouble() * 100) / 100

    fun snapshot(): GlobalDwellBudget {
        return GlobalDwellBudget(
            code = code,
            label = label,
            ceiling = ceiling,
            quantity = quantity,
            loadKg = loadKg,
            unit = unit,
            contactEmail = contactEmail,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a global dwell budget in status idle. */
fun newGlobalDwellBudget(code: String, label: String): GlobalDwellBudget {
    return GlobalDwellBudget(
        code = code,
        label = label,
        ceiling = 0.0,
        quantity = 0,
        loadKg = 0.0,
        unit = "",
        contactEmail = "",
        graceDays = 0,
        status = GlobalDwellBudgetStatus.IDLE,
    )
}
