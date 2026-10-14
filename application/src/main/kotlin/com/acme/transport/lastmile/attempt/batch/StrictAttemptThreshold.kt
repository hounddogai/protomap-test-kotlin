package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict attempt threshold is in its life cycle. */
enum class StrictAttemptThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a strict attempt threshold and its current status. */
data class StrictAttemptThreshold(
    var code: String,
    var label: String,
    var weight: Double,
    var cost: Double,
    var threshold: Double,
    var ratio: Double,
    var timezone: String,
    var status: StrictAttemptThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAttemptThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAttemptThresholdStatus): Boolean {
        when (status) {
            StrictAttemptThresholdStatus.OPEN -> return next == StrictAttemptThresholdStatus.RESOLVED
            StrictAttemptThresholdStatus.IN_REVIEW -> {
                when (next) {
                    StrictAttemptThresholdStatus.RESOLVED -> return true
                }
                return false
            }
            StrictAttemptThresholdStatus.RESOLVED -> {
                when (next) {
                    StrictAttemptThresholdStatus.REOPENED -> return true
                }
                return false
            }
            StrictAttemptThresholdStatus.REOPENED -> {
                when (next) {
                    StrictAttemptThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictAttemptThresholdStatus) {
        if (!canMoveTo(next)) {
            error("strict attempt threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveValue(): Double = round((weight + ratio) * 100) / 100

    fun snapshot(): StrictAttemptThreshold {
        return StrictAttemptThreshold(
            code = code,
            label = label,
            weight = weight,
            cost = cost,
            threshold = threshold,
            ratio = ratio,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict attempt threshold in status open. */
fun newStrictAttemptThreshold(code: String, label: String): StrictAttemptThreshold {
    return StrictAttemptThreshold(
        code = code,
        label = label,
        weight = 0.0,
        cost = 0.0,
        threshold = 0.0,
        ratio = 0.0,
        timezone = "",
        status = StrictAttemptThresholdStatus.OPEN,
    )
}
