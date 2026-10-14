package com.acme.transport.safety.coaching.coachingmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending coaching budget is in its life cycle. */
enum class PendingCoachingBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A pending coaching budget. Its code is unique within a tenant. */
data class PendingCoachingBudget(
    var code: String,
    var label: String,
    var rate: Double,
    var driverId: String,
    var segment: String,
    var reviewer: String,
    var amount: Double,
    var unit: String,
    var limit: Int,
    var status: PendingCoachingBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCoachingBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCoachingBudgetStatus): Boolean {
        when (status) {
            PendingCoachingBudgetStatus.IDLE -> {
                when (next) {
                    PendingCoachingBudgetStatus.READY -> return true
                }
                return false
            }
            PendingCoachingBudgetStatus.WARMING -> {
                when (next) {
                    PendingCoachingBudgetStatus.READY -> return true
                }
                return false
            }
            PendingCoachingBudgetStatus.READY -> {
                when (next) {
                    PendingCoachingBudgetStatus.STOPPED -> return true
                }
                return false
            }
            PendingCoachingBudgetStatus.DRAINING -> {
                when (next) {
                    PendingCoachingBudgetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingCoachingBudgetStatus) {
        if (!canMoveTo(next)) {
            error("pending coaching budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending coaching budget"} $code"

    fun weightedTotal(): Double = round(amount * limit.toDouble() * 100) / 100

    fun snapshot(): PendingCoachingBudget {
        return PendingCoachingBudget(
            code = code,
            label = label,
            rate = rate,
            driverId = driverId,
            segment = segment,
            reviewer = reviewer,
            amount = amount,
            unit = unit,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a pending coaching budget in status idle. */
fun newPendingCoachingBudget(code: String, label: String): PendingCoachingBudget {
    return PendingCoachingBudget(
        code = code,
        label = label,
        rate = 0.0,
        driverId = "",
        segment = "",
        reviewer = "",
        amount = 0.0,
        unit = "",
        limit = 0,
        status = PendingCoachingBudgetStatus.IDLE,
    )
}
