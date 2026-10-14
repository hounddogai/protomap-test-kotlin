package com.acme.transport.customs.inspection.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive inspection estimate is in its life cycle. */
enum class AdaptiveInspectionEstimateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive inspection estimate. Its code is unique within a tenant. */
data class AdaptiveInspectionEstimate(
    var code: String,
    var name: String,
    var amount: Double,
    var attempts: Int,
    var batchSize: Int,
    var notes: String,
    var source: String,
    var timezone: String,
    var summary: String,
    var status: AdaptiveInspectionEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionEstimateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionEstimateStatus): Boolean {
        when (status) {
            AdaptiveInspectionEstimateStatus.PLANNED -> return next == AdaptiveInspectionEstimateStatus.COMPLETED || next == AdaptiveInspectionEstimateStatus.CANCELLED
            AdaptiveInspectionEstimateStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveInspectionEstimateStatus.IN_PROGRESS -> return true
                }
                return false
            }
            AdaptiveInspectionEstimateStatus.IN_PROGRESS -> return next == AdaptiveInspectionEstimateStatus.COMPLETED || next == AdaptiveInspectionEstimateStatus.CANCELLED
            AdaptiveInspectionEstimateStatus.COMPLETED -> {
                when (next) {
                    AdaptiveInspectionEstimateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionEstimateStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round(batchSize.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): AdaptiveInspectionEstimate {
        return AdaptiveInspectionEstimate(
            code = code,
            name = name,
            amount = amount,
            attempts = attempts,
            batchSize = batchSize,
            notes = notes,
            source = source,
            timezone = timezone,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a adaptive inspection estimate in status planned. */
fun newAdaptiveInspectionEstimate(code: String, name: String): AdaptiveInspectionEstimate {
    return AdaptiveInspectionEstimate(
        code = code,
        name = name,
        amount = 0.0,
        attempts = 0,
        batchSize = 0,
        notes = "",
        source = "",
        timezone = "",
        summary = "",
        status = AdaptiveInspectionEstimateStatus.PLANNED,
    )
}
