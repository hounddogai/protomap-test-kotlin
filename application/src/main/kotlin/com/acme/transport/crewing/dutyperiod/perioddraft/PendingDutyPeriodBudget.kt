package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending duty period budget is in its life cycle. */
enum class PendingDutyPeriodBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a pending duty period budget and its current status. */
data class PendingDutyPeriodBudget(
    var code: String,
    var name: String,
    var revision: Int,
    var ceiling: Double,
    var margin: Double,
    var stopCount: Int,
    var sequence: Int,
    var segment: String,
    var cost: Double,
    var status: PendingDutyPeriodBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDutyPeriodBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDutyPeriodBudgetStatus): Boolean {
        when (status) {
            PendingDutyPeriodBudgetStatus.DRAFT -> return next == PendingDutyPeriodBudgetStatus.PENDING || next == PendingDutyPeriodBudgetStatus.ACTIVE
            PendingDutyPeriodBudgetStatus.PENDING -> {
                when (next) {
                    PendingDutyPeriodBudgetStatus.ACTIVE -> return true
                }
                return false
            }
            PendingDutyPeriodBudgetStatus.ACTIVE -> return next == PendingDutyPeriodBudgetStatus.SUSPENDED
            PendingDutyPeriodBudgetStatus.SUSPENDED -> return next == PendingDutyPeriodBudgetStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PendingDutyPeriodBudgetStatus) {
        if (!canMoveTo(next)) {
            error("pending duty period budget $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"pending duty period budget"} $code"

    fun weightedExposure(): Double = round(ceiling * revision.toDouble() * 100) / 100

    fun snapshot(): PendingDutyPeriodBudget {
        return PendingDutyPeriodBudget(
            code = code,
            name = name,
            revision = revision,
            ceiling = ceiling,
            margin = margin,
            stopCount = stopCount,
            sequence = sequence,
            segment = segment,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a pending duty period budget in status draft. */
fun newPendingDutyPeriodBudget(code: String, name: String): PendingDutyPeriodBudget {
    return PendingDutyPeriodBudget(
        code = code,
        name = name,
        revision = 0,
        ceiling = 0.0,
        margin = 0.0,
        stopCount = 0,
        sequence = 0,
        segment = "",
        cost = 0.0,
        status = PendingDutyPeriodBudgetStatus.DRAFT,
    )
}
