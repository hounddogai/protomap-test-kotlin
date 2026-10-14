package com.acme.transport.freightpay.accessorial.accessorialbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual accessorial budget records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a manual accessorial budget is in its life cycle. */
enum class ManualAccessorialBudgetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A manual accessorial budget as the accessorial team keeps it. */
data class ManualAccessorialBudget(
    var code: String,
    var label: String,
    var threshold: Double,
    var createdAt: Int,
    var graceDays: Int,
    var driverId: String,
    var status: ManualAccessorialBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAccessorialBudgetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAccessorialBudgetStatus): Boolean {
        when (status) {
            ManualAccessorialBudgetStatus.PLANNED -> return next == ManualAccessorialBudgetStatus.SCHEDULED
            ManualAccessorialBudgetStatus.SCHEDULED -> {
                when (next) {
                    ManualAccessorialBudgetStatus.COMPLETED, ManualAccessorialBudgetStatus.CANCELLED -> return true
                }
                return false
            }
            ManualAccessorialBudgetStatus.IN_PROGRESS -> return next == ManualAccessorialBudgetStatus.COMPLETED || next == ManualAccessorialBudgetStatus.CANCELLED
            ManualAccessorialBudgetStatus.COMPLETED -> {
                when (next) {
                    ManualAccessorialBudgetStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualAccessorialBudgetStatus) {
        if (!canMoveTo(next)) {
            error("manual accessorial budget $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveVolume(): Double = round((createdAt.toDouble() + threshold) * 100) / 100

    fun snapshot(): ManualAccessorialBudget {
        return ManualAccessorialBudget(
            code = code,
            label = label,
            threshold = threshold,
            createdAt = createdAt,
            graceDays = graceDays,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual accessorial budget in status planned. */
fun newManualAccessorialBudget(code: String, label: String): ManualAccessorialBudget {
    return ManualAccessorialBudget(
        code = code,
        label = label,
        threshold = 0.0,
        createdAt = 0,
        graceDays = 0,
        driverId = "",
        status = ManualAccessorialBudgetStatus.PLANNED,
    )
}
