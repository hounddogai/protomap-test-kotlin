package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal coaching variance is in its life cycle. */
enum class SeasonalCoachingVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a seasonal coaching variance and its current status. */
data class SeasonalCoachingVariance(
    var code: String,
    var label: String,
    var contactEmail: String,
    var timezone: String,
    var ratio: Double,
    var threshold: Double,
    var ownerId: String,
    var summary: String,
    var status: SeasonalCoachingVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCoachingVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCoachingVarianceStatus): Boolean {
        when (status) {
            SeasonalCoachingVarianceStatus.PLANNED -> return next == SeasonalCoachingVarianceStatus.SCHEDULED
            SeasonalCoachingVarianceStatus.SCHEDULED -> {
                when (next) {
                    SeasonalCoachingVarianceStatus.IN_PROGRESS, SeasonalCoachingVarianceStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalCoachingVarianceStatus.IN_PROGRESS -> {
                when (next) {
                    SeasonalCoachingVarianceStatus.COMPLETED, SeasonalCoachingVarianceStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalCoachingVarianceStatus.COMPLETED -> return next == SeasonalCoachingVarianceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalCoachingVarianceStatus) {
        if (!canMoveTo(next)) {
            error("seasonal coaching variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedTotal(): Double = round((ratio - threshold) * 100) / 100

    fun snapshot(): SeasonalCoachingVariance {
        return SeasonalCoachingVariance(
            code = code,
            label = label,
            contactEmail = contactEmail,
            timezone = timezone,
            ratio = ratio,
            threshold = threshold,
            ownerId = ownerId,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal coaching variance in status planned. */
fun newSeasonalCoachingVariance(code: String, label: String): SeasonalCoachingVariance {
    return SeasonalCoachingVariance(
        code = code,
        label = label,
        contactEmail = "",
        timezone = "",
        ratio = 0.0,
        threshold = 0.0,
        ownerId = "",
        summary = "",
        status = SeasonalCoachingVarianceStatus.PLANNED,
    )
}
