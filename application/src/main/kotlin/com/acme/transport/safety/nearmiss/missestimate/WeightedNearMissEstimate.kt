package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted near miss estimate records of safety's near miss work, and the rules, stores, and calculations
// around them.

/** Where a weighted near miss estimate is in its life cycle. */
enum class WeightedNearMissEstimateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One weighted near miss estimate, with what the planners need to act on it. */
data class WeightedNearMissEstimate(
    var code: String,
    var title: String,
    var locale: String,
    var ratio: Double,
    var graceDays: Int,
    var capacity: Int,
    var reviewer: String,
    var fuelLiters: Double,
    var revision: Int,
    var status: WeightedNearMissEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedNearMissEstimateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedNearMissEstimateStatus): Boolean {
        when (status) {
            WeightedNearMissEstimateStatus.PLANNED -> {
                when (next) {
                    WeightedNearMissEstimateStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedNearMissEstimateStatus.SCHEDULED -> {
                when (next) {
                    WeightedNearMissEstimateStatus.IN_PROGRESS, WeightedNearMissEstimateStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedNearMissEstimateStatus.IN_PROGRESS -> return next == WeightedNearMissEstimateStatus.COMPLETED || next == WeightedNearMissEstimateStatus.CANCELLED
            WeightedNearMissEstimateStatus.COMPLETED -> {
                when (next) {
                    WeightedNearMissEstimateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedNearMissEstimateStatus) {
        if (!canMoveTo(next)) {
            error("weighted near miss estimate $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun weightedLoad(): Double = round((graceDays.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): WeightedNearMissEstimate {
        return WeightedNearMissEstimate(
            code = code,
            title = title,
            locale = locale,
            ratio = ratio,
            graceDays = graceDays,
            capacity = capacity,
            reviewer = reviewer,
            fuelLiters = fuelLiters,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a weighted near miss estimate in status planned. */
fun newWeightedNearMissEstimate(code: String, title: String): WeightedNearMissEstimate {
    return WeightedNearMissEstimate(
        code = code,
        title = title,
        locale = "",
        ratio = 0.0,
        graceDays = 0,
        capacity = 0,
        reviewer = "",
        fuelLiters = 0.0,
        revision = 0,
        status = WeightedNearMissEstimateStatus.PLANNED,
    )
}
