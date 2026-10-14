package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.kit.text.normalize

/** Where a rolling attempt threshold is in its life cycle. */
enum class RollingAttemptThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a rolling attempt threshold and its current status. */
data class RollingAttemptThreshold(
    var code: String,
    var label: String,
    var carrier: String,
    var notes: String,
    var batchSize: Int,
    var status: RollingAttemptThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAttemptThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAttemptThresholdStatus): Boolean {
        when (status) {
            RollingAttemptThresholdStatus.PLANNED -> {
                when (next) {
                    RollingAttemptThresholdStatus.IN_PROGRESS, RollingAttemptThresholdStatus.COMPLETED -> return true
                }
                return false
            }
            RollingAttemptThresholdStatus.SCHEDULED -> return next == RollingAttemptThresholdStatus.COMPLETED
            RollingAttemptThresholdStatus.IN_PROGRESS -> return next == RollingAttemptThresholdStatus.COMPLETED
            RollingAttemptThresholdStatus.COMPLETED -> {
                when (next) {
                    RollingAttemptThresholdStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingAttemptThresholdStatus) {
        if (!canMoveTo(next)) {
            error("rolling attempt threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): RollingAttemptThreshold {
        return RollingAttemptThreshold(
            code = code,
            label = label,
            carrier = carrier,
            notes = notes,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling attempt threshold in status planned. */
fun newRollingAttemptThreshold(code: String, label: String): RollingAttemptThreshold {
    return RollingAttemptThreshold(
        code = code,
        label = label,
        carrier = "",
        notes = "",
        batchSize = 0,
        status = RollingAttemptThresholdStatus.PLANNED,
    )
}
