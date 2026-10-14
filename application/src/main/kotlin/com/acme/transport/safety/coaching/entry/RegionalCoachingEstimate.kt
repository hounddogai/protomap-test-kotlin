package com.acme.transport.safety.coaching.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional coaching estimate is in its life cycle. */
enum class RegionalCoachingEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional coaching estimate. Its code is unique within a tenant. */
data class RegionalCoachingEstimate(
    var code: String,
    var name: String,
    var batchSize: Int,
    var threshold: Double,
    var odometer: Int,
    var startsAt: Int,
    var unit: String,
    var reviewer: String,
    var summary: String,
    var priority: Int,
    var attempts: Int,
    var status: RegionalCoachingEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCoachingEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCoachingEstimateStatus): Boolean {
        when (status) {
            RegionalCoachingEstimateStatus.CREATED -> return next == RegionalCoachingEstimateStatus.RETIRED
            RegionalCoachingEstimateStatus.VALIDATED -> {
                when (next) {
                    RegionalCoachingEstimateStatus.RETIRED -> return true
                }
                return false
            }
            RegionalCoachingEstimateStatus.PUBLISHED -> {
                when (next) {
                    RegionalCoachingEstimateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCoachingEstimateStatus) {
        if (!canMoveTo(next)) {
            error("regional coaching estimate $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"regional coaching estimate"} $code"

    fun projectedExposure(): Double = round((threshold + priority.toDouble()) * 100) / 100

    fun snapshot(): RegionalCoachingEstimate {
        return RegionalCoachingEstimate(
            code = code,
            name = name,
            batchSize = batchSize,
            threshold = threshold,
            odometer = odometer,
            startsAt = startsAt,
            unit = unit,
            reviewer = reviewer,
            summary = summary,
            priority = priority,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a regional coaching estimate in status created. */
fun newRegionalCoachingEstimate(code: String, name: String): RegionalCoachingEstimate {
    return RegionalCoachingEstimate(
        code = code,
        name = name,
        batchSize = 0,
        threshold = 0.0,
        odometer = 0,
        startsAt = 0,
        unit = "",
        reviewer = "",
        summary = "",
        priority = 0,
        attempts = 0,
        status = RegionalCoachingEstimateStatus.CREATED,
    )
}
