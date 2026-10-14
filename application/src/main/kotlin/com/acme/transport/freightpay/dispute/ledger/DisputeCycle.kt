package com.acme.transport.freightpay.dispute.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute cycle is in its life cycle. */
enum class DisputeCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One dispute cycle, with what the planners need to act on it. */
data class DisputeCycle(
    var code: String,
    var label: String,
    var amount: Double,
    var weight: Double,
    var revision: Int,
    var name: String,
    var category: String,
    var status: DisputeCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeCycleStatus): Boolean {
        when (status) {
            DisputeCycleStatus.PLANNED -> return next == DisputeCycleStatus.COMPLETED || next == DisputeCycleStatus.CANCELLED
            DisputeCycleStatus.SCHEDULED -> return next == DisputeCycleStatus.COMPLETED || next == DisputeCycleStatus.CANCELLED
            DisputeCycleStatus.IN_PROGRESS -> {
                when (next) {
                    DisputeCycleStatus.COMPLETED -> return true
                }
                return false
            }
            DisputeCycleStatus.COMPLETED -> return next == DisputeCycleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DisputeCycleStatus) {
        if (!canMoveTo(next)) {
            error("dispute cycle $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedTotal(): Double = round(weight * amount * 100) / 100

    fun snapshot(): DisputeCycle {
        return DisputeCycle(
            code = code,
            label = label,
            amount = amount,
            weight = weight,
            revision = revision,
            name = name,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a dispute cycle in status planned. */
fun newDisputeCycle(code: String, label: String): DisputeCycle {
    return DisputeCycle(
        code = code,
        label = label,
        amount = 0.0,
        weight = 0.0,
        revision = 0,
        name = "",
        category = "",
        status = DisputeCycleStatus.PLANNED,
    )
}
