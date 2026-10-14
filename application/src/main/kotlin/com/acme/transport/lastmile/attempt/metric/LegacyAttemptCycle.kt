package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.text.normalize

/** Where a legacy attempt cycle is in its life cycle. */
enum class LegacyAttemptCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a legacy attempt cycle and its current status. */
data class LegacyAttemptCycle(
    var code: String,
    var title: String,
    var externalRef: String,
    var channel: String,
    var attempts: Int,
    var notes: String,
    var status: LegacyAttemptCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyAttemptCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyAttemptCycleStatus): Boolean {
        when (status) {
            LegacyAttemptCycleStatus.DRAFT -> {
                when (next) {
                    LegacyAttemptCycleStatus.PENDING, LegacyAttemptCycleStatus.SUSPENDED -> return true
                }
                return false
            }
            LegacyAttemptCycleStatus.PENDING -> return next == LegacyAttemptCycleStatus.ACTIVE || next == LegacyAttemptCycleStatus.SUSPENDED
            LegacyAttemptCycleStatus.ACTIVE -> return next == LegacyAttemptCycleStatus.CLOSED
            LegacyAttemptCycleStatus.SUSPENDED -> {
                when (next) {
                    LegacyAttemptCycleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyAttemptCycleStatus) {
        if (!canMoveTo(next)) {
            error("legacy attempt cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): LegacyAttemptCycle {
        return LegacyAttemptCycle(
            code = code,
            title = title,
            externalRef = externalRef,
            channel = channel,
            attempts = attempts,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy attempt cycle in status draft. */
fun newLegacyAttemptCycle(code: String, title: String): LegacyAttemptCycle {
    return LegacyAttemptCycle(
        code = code,
        title = title,
        externalRef = "",
        channel = "",
        attempts = 0,
        notes = "",
        status = LegacyAttemptCycleStatus.DRAFT,
    )
}
