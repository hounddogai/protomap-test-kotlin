package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered defect variance is in its life cycle. */
enum class TieredDefectVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a tiered defect variance and its current status. */
data class TieredDefectVariance(
    var code: String,
    var label: String,
    var budget: Double,
    var durationMinutes: Int,
    var version: Int,
    var phone: String,
    var revision: Int,
    var trailerId: String,
    var status: TieredDefectVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDefectVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDefectVarianceStatus): Boolean {
        when (status) {
            TieredDefectVarianceStatus.PLANNED -> {
                when (next) {
                    TieredDefectVarianceStatus.IN_PROGRESS -> return true
                }
                return false
            }
            TieredDefectVarianceStatus.SCHEDULED -> return next == TieredDefectVarianceStatus.COMPLETED || next == TieredDefectVarianceStatus.CANCELLED
            TieredDefectVarianceStatus.IN_PROGRESS -> return next == TieredDefectVarianceStatus.COMPLETED
            TieredDefectVarianceStatus.COMPLETED -> {
                when (next) {
                    TieredDefectVarianceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredDefectVarianceStatus) {
        if (!canMoveTo(next)) {
            error("tiered defect variance $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"tiered defect variance"} $code"

    fun adjustedExposure(): Double = round((budget - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): TieredDefectVariance {
        return TieredDefectVariance(
            code = code,
            label = label,
            budget = budget,
            durationMinutes = durationMinutes,
            version = version,
            phone = phone,
            revision = revision,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered defect variance in status planned. */
fun newTieredDefectVariance(code: String, label: String): TieredDefectVariance {
    return TieredDefectVariance(
        code = code,
        label = label,
        budget = 0.0,
        durationMinutes = 0,
        version = 0,
        phone = "",
        revision = 0,
        trailerId = "",
        status = TieredDefectVarianceStatus.PLANNED,
    )
}
