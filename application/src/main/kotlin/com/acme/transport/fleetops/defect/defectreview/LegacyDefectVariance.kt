package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy defect variance is in its life cycle. */
enum class LegacyDefectVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a legacy defect variance and its current status. */
data class LegacyDefectVariance(
    var code: String,
    var title: String,
    var budget: Double,
    var distanceKm: Double,
    var depot: String,
    var ceiling: Double,
    var graceDays: Int,
    var durationMinutes: Int,
    var status: LegacyDefectVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDefectVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDefectVarianceStatus): Boolean {
        when (status) {
            LegacyDefectVarianceStatus.PLANNED -> return next == LegacyDefectVarianceStatus.IN_PROGRESS || next == LegacyDefectVarianceStatus.CANCELLED
            LegacyDefectVarianceStatus.SCHEDULED -> return next == LegacyDefectVarianceStatus.CANCELLED
            LegacyDefectVarianceStatus.IN_PROGRESS -> return next == LegacyDefectVarianceStatus.CANCELLED
            LegacyDefectVarianceStatus.COMPLETED -> return next == LegacyDefectVarianceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyDefectVarianceStatus) {
        if (!canMoveTo(next)) {
            error("legacy defect variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedLoad(): Double = round((durationMinutes.toDouble() - budget) * 100) / 100

    fun snapshot(): LegacyDefectVariance {
        return LegacyDefectVariance(
            code = code,
            title = title,
            budget = budget,
            distanceKm = distanceKm,
            depot = depot,
            ceiling = ceiling,
            graceDays = graceDays,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a legacy defect variance in status planned. */
fun newLegacyDefectVariance(code: String, title: String): LegacyDefectVariance {
    return LegacyDefectVariance(
        code = code,
        title = title,
        budget = 0.0,
        distanceKm = 0.0,
        depot = "",
        ceiling = 0.0,
        graceDays = 0,
        durationMinutes = 0,
        status = LegacyDefectVarianceStatus.PLANNED,
    )
}
