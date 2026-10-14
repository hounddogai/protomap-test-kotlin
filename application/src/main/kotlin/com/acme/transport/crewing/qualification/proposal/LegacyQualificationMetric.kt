package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy qualification metric is in its life cycle. */
enum class LegacyQualificationMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy qualification metric as the qualification team keeps it. */
data class LegacyQualificationMetric(
    var code: String,
    var label: String,
    var notes: String,
    var score: Double,
    var updatedAt: Int,
    var pallets: Int,
    var driverId: String,
    var externalRef: String,
    var amount: Double,
    var attempts: Int,
    var status: LegacyQualificationMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyQualificationMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyQualificationMetricStatus): Boolean {
        when (status) {
            LegacyQualificationMetricStatus.PLANNED -> return next == LegacyQualificationMetricStatus.SCHEDULED
            LegacyQualificationMetricStatus.SCHEDULED -> {
                when (next) {
                    LegacyQualificationMetricStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyQualificationMetricStatus.IN_PROGRESS -> return next == LegacyQualificationMetricStatus.COMPLETED || next == LegacyQualificationMetricStatus.CANCELLED
            LegacyQualificationMetricStatus.COMPLETED -> {
                when (next) {
                    LegacyQualificationMetricStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyQualificationMetricStatus) {
        if (!canMoveTo(next)) {
            error("legacy qualification metric $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"legacy qualification metric"} $code"

    fun projectedExposure(): Double = round((updatedAt.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): LegacyQualificationMetric {
        return LegacyQualificationMetric(
            code = code,
            label = label,
            notes = notes,
            score = score,
            updatedAt = updatedAt,
            pallets = pallets,
            driverId = driverId,
            externalRef = externalRef,
            amount = amount,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy qualification metric in status planned. */
fun newLegacyQualificationMetric(code: String, label: String): LegacyQualificationMetric {
    return LegacyQualificationMetric(
        code = code,
        label = label,
        notes = "",
        score = 0.0,
        updatedAt = 0,
        pallets = 0,
        driverId = "",
        externalRef = "",
        amount = 0.0,
        attempts = 0,
        status = LegacyQualificationMetricStatus.PLANNED,
    )
}
