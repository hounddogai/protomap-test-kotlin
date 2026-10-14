package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling duty period budget is in its life cycle. */
enum class RollingDutyPeriodBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rolling duty period budget as the duty period team keeps it. */
data class RollingDutyPeriodBudget(
    var code: String,
    var title: String,
    var phone: String,
    var attempts: Int,
    var checksum: String,
    var notes: String,
    var lane: String,
    var priority: Int,
    var batchSize: Int,
    var ownerId: String,
    var visible: Boolean,
    var status: RollingDutyPeriodBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDutyPeriodBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDutyPeriodBudgetStatus): Boolean {
        when (status) {
            RollingDutyPeriodBudgetStatus.DRAFT -> return next == RollingDutyPeriodBudgetStatus.ACTIVE || next == RollingDutyPeriodBudgetStatus.CLOSED
            RollingDutyPeriodBudgetStatus.PENDING -> return next == RollingDutyPeriodBudgetStatus.ACTIVE
            RollingDutyPeriodBudgetStatus.ACTIVE -> {
                when (next) {
                    RollingDutyPeriodBudgetStatus.SUSPENDED -> return true
                }
                return false
            }
            RollingDutyPeriodBudgetStatus.SUSPENDED -> return next == RollingDutyPeriodBudgetStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RollingDutyPeriodBudgetStatus) {
        if (!canMoveTo(next)) {
            error("rolling duty period budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling duty period budget"} $code"

    fun adjustedExposure(): Double = round((attempts.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): RollingDutyPeriodBudget {
        return RollingDutyPeriodBudget(
            code = code,
            title = title,
            phone = phone,
            attempts = attempts,
            checksum = checksum,
            notes = notes,
            lane = lane,
            priority = priority,
            batchSize = batchSize,
            ownerId = ownerId,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rolling duty period budget in status draft. */
fun newRollingDutyPeriodBudget(code: String, title: String): RollingDutyPeriodBudget {
    return RollingDutyPeriodBudget(
        code = code,
        title = title,
        phone = "",
        attempts = 0,
        checksum = "",
        notes = "",
        lane = "",
        priority = 0,
        batchSize = 0,
        ownerId = "",
        visible = false,
        status = RollingDutyPeriodBudgetStatus.DRAFT,
    )
}
