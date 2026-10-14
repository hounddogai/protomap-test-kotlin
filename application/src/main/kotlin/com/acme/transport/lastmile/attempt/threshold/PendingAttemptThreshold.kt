package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending attempt threshold is in its life cycle. */
enum class PendingAttemptThresholdStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a pending attempt threshold and its current status. */
data class PendingAttemptThreshold(
    var code: String,
    var name: String,
    var batchSize: Int,
    var score: Double,
    var unit: String,
    var label: String,
    var status: PendingAttemptThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAttemptThresholdStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAttemptThresholdStatus): Boolean {
        when (status) {
            PendingAttemptThresholdStatus.DRAFT -> return next == PendingAttemptThresholdStatus.SUSPENDED || next == PendingAttemptThresholdStatus.CLOSED
            PendingAttemptThresholdStatus.PENDING -> {
                when (next) {
                    PendingAttemptThresholdStatus.SUSPENDED -> return true
                }
                return false
            }
            PendingAttemptThresholdStatus.ACTIVE -> {
                when (next) {
                    PendingAttemptThresholdStatus.SUSPENDED, PendingAttemptThresholdStatus.CLOSED -> return true
                }
                return false
            }
            PendingAttemptThresholdStatus.SUSPENDED -> return next == PendingAttemptThresholdStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PendingAttemptThresholdStatus) {
        if (!canMoveTo(next)) {
            error("pending attempt threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($name)"

    fun effectiveValue(): Double = round(batchSize.toDouble() * score * 100) / 100

    fun snapshot(): PendingAttemptThreshold {
        return PendingAttemptThreshold(
            code = code,
            name = name,
            batchSize = batchSize,
            score = score,
            unit = unit,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending attempt threshold in status draft. */
fun newPendingAttemptThreshold(code: String, name: String): PendingAttemptThreshold {
    return PendingAttemptThreshold(
        code = code,
        name = name,
        batchSize = 0,
        score = 0.0,
        unit = "",
        label = "",
        status = PendingAttemptThresholdStatus.DRAFT,
    )
}
