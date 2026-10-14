package com.acme.transport.lastmile.attempt.plan

import com.acme.transport.kit.text.normalize

/** Where a tiered attempt baseline is in its life cycle. */
enum class TieredAttemptBaselineStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One tiered attempt baseline, with what the planners need to act on it. */
data class TieredAttemptBaseline(
    var code: String,
    var label: String,
    var lane: String,
    var carrier: String,
    var title: String,
    var amount: Double,
    var status: TieredAttemptBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredAttemptBaselineStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredAttemptBaselineStatus): Boolean {
        when (status) {
            TieredAttemptBaselineStatus.NEW -> return next == TieredAttemptBaselineStatus.QUEUED
            TieredAttemptBaselineStatus.QUEUED -> {
                when (next) {
                    TieredAttemptBaselineStatus.DONE, TieredAttemptBaselineStatus.FAILED -> return true
                }
                return false
            }
            TieredAttemptBaselineStatus.RUNNING -> return next == TieredAttemptBaselineStatus.FAILED
            TieredAttemptBaselineStatus.DONE -> {
                when (next) {
                    TieredAttemptBaselineStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredAttemptBaselineStatus) {
        if (!canMoveTo(next)) {
            error("tiered attempt baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tiered attempt baseline"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): TieredAttemptBaseline {
        return TieredAttemptBaseline(
            code = code,
            label = label,
            lane = lane,
            carrier = carrier,
            title = title,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tiered attempt baseline in status new. */
fun newTieredAttemptBaseline(code: String, label: String): TieredAttemptBaseline {
    return TieredAttemptBaseline(
        code = code,
        label = label,
        lane = "",
        carrier = "",
        title = "",
        amount = 0.0,
        status = TieredAttemptBaselineStatus.NEW,
    )
}
