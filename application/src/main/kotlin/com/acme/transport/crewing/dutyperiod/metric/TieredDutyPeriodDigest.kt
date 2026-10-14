package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered duty period digest is in its life cycle. */
enum class TieredDutyPeriodDigestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One tiered duty period digest, with what the planners need to act on it. */
data class TieredDutyPeriodDigest(
    var code: String,
    var name: String,
    var margin: Double,
    var budget: Double,
    var attempts: Int,
    var segment: String,
    var status: TieredDutyPeriodDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDutyPeriodDigestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDutyPeriodDigestStatus): Boolean {
        when (status) {
            TieredDutyPeriodDigestStatus.PLANNED -> {
                when (next) {
                    TieredDutyPeriodDigestStatus.CANCELLED -> return true
                }
                return false
            }
            TieredDutyPeriodDigestStatus.SCHEDULED -> {
                when (next) {
                    TieredDutyPeriodDigestStatus.IN_PROGRESS -> return true
                }
                return false
            }
            TieredDutyPeriodDigestStatus.IN_PROGRESS -> {
                when (next) {
                    TieredDutyPeriodDigestStatus.COMPLETED, TieredDutyPeriodDigestStatus.CANCELLED -> return true
                }
                return false
            }
            TieredDutyPeriodDigestStatus.COMPLETED -> {
                when (next) {
                    TieredDutyPeriodDigestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredDutyPeriodDigestStatus) {
        if (!canMoveTo(next)) {
            error("tiered duty period digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedLoad(): Double = round(budget * attempts.toDouble() * 100) / 100

    fun snapshot(): TieredDutyPeriodDigest {
        return TieredDutyPeriodDigest(
            code = code,
            name = name,
            margin = margin,
            budget = budget,
            attempts = attempts,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tiered duty period digest in status planned. */
fun newTieredDutyPeriodDigest(code: String, name: String): TieredDutyPeriodDigest {
    return TieredDutyPeriodDigest(
        code = code,
        name = name,
        margin = 0.0,
        budget = 0.0,
        attempts = 0,
        segment = "",
        status = TieredDutyPeriodDigestStatus.PLANNED,
    )
}
