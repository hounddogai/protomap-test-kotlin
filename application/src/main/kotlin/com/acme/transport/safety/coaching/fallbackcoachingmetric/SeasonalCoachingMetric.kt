package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal coaching metric records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a seasonal coaching metric is in its life cycle. */
enum class SeasonalCoachingMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One seasonal coaching metric, with what the planners need to act on it. */
data class SeasonalCoachingMetric(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var odometer: Int,
    var category: String,
    var amount: Double,
    var archived: Boolean,
    var status: SeasonalCoachingMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCoachingMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCoachingMetricStatus): Boolean {
        when (status) {
            SeasonalCoachingMetricStatus.REQUESTED -> {
                when (next) {
                    SeasonalCoachingMetricStatus.REVOKED, SeasonalCoachingMetricStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalCoachingMetricStatus.GRANTED -> {
                when (next) {
                    SeasonalCoachingMetricStatus.REVOKED, SeasonalCoachingMetricStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalCoachingMetricStatus.REVOKED -> return next == SeasonalCoachingMetricStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SeasonalCoachingMetricStatus) {
        if (!canMoveTo(next)) {
            error("seasonal coaching metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal coaching metric"} $code"

    fun weightedValue(): Double = round((odometer.toDouble() + amount) * 100) / 100

    fun snapshot(): SeasonalCoachingMetric {
        return SeasonalCoachingMetric(
            code = code,
            label = label,
            pinned = pinned,
            odometer = odometer,
            category = category,
            amount = amount,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a seasonal coaching metric in status requested. */
fun newSeasonalCoachingMetric(code: String, label: String): SeasonalCoachingMetric {
    return SeasonalCoachingMetric(
        code = code,
        label = label,
        pinned = false,
        odometer = 0,
        category = "",
        amount = 0.0,
        archived = false,
        status = SeasonalCoachingMetricStatus.REQUESTED,
    )
}
