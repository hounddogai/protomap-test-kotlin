package com.acme.transport.crewing.roster.regionalrostersummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered roster plan is in its life cycle. */
enum class TieredRosterPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One tiered roster plan, with what the planners need to act on it. */
data class TieredRosterPlan(
    var code: String,
    var name: String,
    var budget: Double,
    var amount: Double,
    var depot: String,
    var durationMinutes: Int,
    var status: TieredRosterPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRosterPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRosterPlanStatus): Boolean {
        when (status) {
            TieredRosterPlanStatus.IDLE -> return next == TieredRosterPlanStatus.READY || next == TieredRosterPlanStatus.DRAINING
            TieredRosterPlanStatus.WARMING -> {
                when (next) {
                    TieredRosterPlanStatus.STOPPED -> return true
                }
                return false
            }
            TieredRosterPlanStatus.READY -> {
                when (next) {
                    TieredRosterPlanStatus.DRAINING, TieredRosterPlanStatus.STOPPED -> return true
                }
                return false
            }
            TieredRosterPlanStatus.DRAINING -> {
                when (next) {
                    TieredRosterPlanStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredRosterPlanStatus) {
        if (!canMoveTo(next)) {
            error("tiered roster plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveValue(): Double = round((durationMinutes.toDouble() + budget) * 100) / 100

    fun snapshot(): TieredRosterPlan {
        return TieredRosterPlan(
            code = code,
            name = name,
            budget = budget,
            amount = amount,
            depot = depot,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tiered roster plan in status idle. */
fun newTieredRosterPlan(code: String, name: String): TieredRosterPlan {
    return TieredRosterPlan(
        code = code,
        name = name,
        budget = 0.0,
        amount = 0.0,
        depot = "",
        durationMinutes = 0,
        status = TieredRosterPlanStatus.IDLE,
    )
}
