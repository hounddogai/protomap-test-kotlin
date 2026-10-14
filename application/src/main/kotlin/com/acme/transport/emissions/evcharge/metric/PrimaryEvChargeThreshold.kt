package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary ev charge threshold is in its life cycle. */
enum class PrimaryEvChargeThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One primary ev charge threshold, with what the planners need to act on it. */
data class PrimaryEvChargeThreshold(
    var code: String,
    var name: String,
    var unit: String,
    var priority: Int,
    var score: Double,
    var status: PrimaryEvChargeThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryEvChargeThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryEvChargeThresholdStatus): Boolean {
        when (status) {
            PrimaryEvChargeThresholdStatus.OPEN -> {
                when (next) {
                    PrimaryEvChargeThresholdStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryEvChargeThresholdStatus.IN_REVIEW -> return next == PrimaryEvChargeThresholdStatus.REOPENED || next == PrimaryEvChargeThresholdStatus.ARCHIVED
            PrimaryEvChargeThresholdStatus.RESOLVED -> {
                when (next) {
                    PrimaryEvChargeThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryEvChargeThresholdStatus.REOPENED -> return next == PrimaryEvChargeThresholdStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryEvChargeThresholdStatus) {
        if (!canMoveTo(next)) {
            error("primary ev charge threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedValue(): Double = round((priority.toDouble() - score) * 100) / 100

    fun snapshot(): PrimaryEvChargeThreshold {
        return PrimaryEvChargeThreshold(
            code = code,
            name = name,
            unit = unit,
            priority = priority,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a primary ev charge threshold in status open. */
fun newPrimaryEvChargeThreshold(code: String, name: String): PrimaryEvChargeThreshold {
    return PrimaryEvChargeThreshold(
        code = code,
        name = name,
        unit = "",
        priority = 0,
        score = 0.0,
        status = PrimaryEvChargeThresholdStatus.OPEN,
    )
}
