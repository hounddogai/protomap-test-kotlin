package com.acme.transport.crewing.availability.availabilitydigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual availability budget is in its life cycle. */
enum class ManualAvailabilityBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual availability budget. Its code is unique within a tenant. */
data class ManualAvailabilityBudget(
    var code: String,
    var label: String,
    var discount: Double,
    var priority: Int,
    var category: String,
    var minimum: Double,
    var status: ManualAvailabilityBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAvailabilityBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAvailabilityBudgetStatus): Boolean {
        when (status) {
            ManualAvailabilityBudgetStatus.IDLE -> {
                when (next) {
                    ManualAvailabilityBudgetStatus.STOPPED -> return true
                }
                return false
            }
            ManualAvailabilityBudgetStatus.WARMING -> return next == ManualAvailabilityBudgetStatus.READY || next == ManualAvailabilityBudgetStatus.STOPPED
            ManualAvailabilityBudgetStatus.READY -> {
                when (next) {
                    ManualAvailabilityBudgetStatus.STOPPED -> return true
                }
                return false
            }
            ManualAvailabilityBudgetStatus.DRAINING -> {
                when (next) {
                    ManualAvailabilityBudgetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualAvailabilityBudgetStatus) {
        if (!canMoveTo(next)) {
            error("manual availability budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual availability budget"} $code"

    fun netExposure(): Double = round((priority.toDouble() - minimum) * 100) / 100

    fun snapshot(): ManualAvailabilityBudget {
        return ManualAvailabilityBudget(
            code = code,
            label = label,
            discount = discount,
            priority = priority,
            category = category,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a manual availability budget in status idle. */
fun newManualAvailabilityBudget(code: String, label: String): ManualAvailabilityBudget {
    return ManualAvailabilityBudget(
        code = code,
        label = label,
        discount = 0.0,
        priority = 0,
        category = "",
        minimum = 0.0,
        status = ManualAvailabilityBudgetStatus.IDLE,
    )
}
