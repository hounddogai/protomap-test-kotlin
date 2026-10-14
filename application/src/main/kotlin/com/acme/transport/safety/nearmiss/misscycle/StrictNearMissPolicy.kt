package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict near miss policy is in its life cycle. */
enum class StrictNearMissPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict near miss policy. Its code is unique within a tenant. */
data class StrictNearMissPolicy(
    var code: String,
    var label: String,
    var graceDays: Int,
    var revision: Int,
    var maximum: Double,
    var status: StrictNearMissPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictNearMissPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictNearMissPolicyStatus): Boolean {
        when (status) {
            StrictNearMissPolicyStatus.OPEN -> return next == StrictNearMissPolicyStatus.RESOLVED
            StrictNearMissPolicyStatus.IN_REVIEW -> {
                when (next) {
                    StrictNearMissPolicyStatus.REOPENED -> return true
                }
                return false
            }
            StrictNearMissPolicyStatus.RESOLVED -> return next == StrictNearMissPolicyStatus.REOPENED || next == StrictNearMissPolicyStatus.ARCHIVED
            StrictNearMissPolicyStatus.REOPENED -> {
                when (next) {
                    StrictNearMissPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictNearMissPolicyStatus) {
        if (!canMoveTo(next)) {
            error("strict near miss policy $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedTotal(): Double = round((graceDays.toDouble() + maximum) * 100) / 100

    fun snapshot(): StrictNearMissPolicy {
        return StrictNearMissPolicy(
            code = code,
            label = label,
            graceDays = graceDays,
            revision = revision,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict near miss policy in status open. */
fun newStrictNearMissPolicy(code: String, label: String): StrictNearMissPolicy {
    return StrictNearMissPolicy(
        code = code,
        label = label,
        graceDays = 0,
        revision = 0,
        maximum = 0.0,
        status = StrictNearMissPolicyStatus.OPEN,
    )
}
