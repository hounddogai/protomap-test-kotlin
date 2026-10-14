package com.acme.transport.freightpay.accessorial.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending accessorial budget is in its life cycle. */
enum class PendingAccessorialBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One pending accessorial budget, with what the planners need to act on it. */
data class PendingAccessorialBudget(
    var code: String,
    var name: String,
    var threshold: Double,
    var lane: String,
    var quantity: Int,
    var priority: Int,
    var status: PendingAccessorialBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAccessorialBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAccessorialBudgetStatus): Boolean {
        when (status) {
            PendingAccessorialBudgetStatus.CREATED -> {
                when (next) {
                    PendingAccessorialBudgetStatus.PUBLISHED -> return true
                }
                return false
            }
            PendingAccessorialBudgetStatus.VALIDATED -> return next == PendingAccessorialBudgetStatus.PUBLISHED || next == PendingAccessorialBudgetStatus.RETIRED
            PendingAccessorialBudgetStatus.PUBLISHED -> return next == PendingAccessorialBudgetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PendingAccessorialBudgetStatus) {
        if (!canMoveTo(next)) {
            error("pending accessorial budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedExposure(): Double = round((threshold - priority.toDouble()) * 100) / 100

    fun snapshot(): PendingAccessorialBudget {
        return PendingAccessorialBudget(
            code = code,
            name = name,
            threshold = threshold,
            lane = lane,
            quantity = quantity,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a pending accessorial budget in status created. */
fun newPendingAccessorialBudget(code: String, name: String): PendingAccessorialBudget {
    return PendingAccessorialBudget(
        code = code,
        name = name,
        threshold = 0.0,
        lane = "",
        quantity = 0,
        priority = 0,
        status = PendingAccessorialBudgetStatus.CREATED,
    )
}
