package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled attempt slot is in its life cycle. */
enum class ScheduledAttemptSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a scheduled attempt slot and its current status. */
data class ScheduledAttemptSlot(
    var code: String,
    var label: String,
    var description: String,
    var cost: Double,
    var amount: Double,
    var graceDays: Int,
    var lane: String,
    var status: ScheduledAttemptSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAttemptSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAttemptSlotStatus): Boolean {
        when (status) {
            ScheduledAttemptSlotStatus.OPEN -> return next == ScheduledAttemptSlotStatus.IN_REVIEW || next == ScheduledAttemptSlotStatus.RESOLVED
            ScheduledAttemptSlotStatus.IN_REVIEW -> return next == ScheduledAttemptSlotStatus.REOPENED
            ScheduledAttemptSlotStatus.RESOLVED -> return next == ScheduledAttemptSlotStatus.REOPENED || next == ScheduledAttemptSlotStatus.ARCHIVED
            ScheduledAttemptSlotStatus.REOPENED -> return next == ScheduledAttemptSlotStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledAttemptSlotStatus) {
        if (!canMoveTo(next)) {
            error("scheduled attempt slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedExposure(): Double = round((amount - cost) * 100) / 100

    fun snapshot(): ScheduledAttemptSlot {
        return ScheduledAttemptSlot(
            code = code,
            label = label,
            description = description,
            cost = cost,
            amount = amount,
            graceDays = graceDays,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scheduled attempt slot in status open. */
fun newScheduledAttemptSlot(code: String, label: String): ScheduledAttemptSlot {
    return ScheduledAttemptSlot(
        code = code,
        label = label,
        description = "",
        cost = 0.0,
        amount = 0.0,
        graceDays = 0,
        lane = "",
        status = ScheduledAttemptSlotStatus.OPEN,
    )
}
