package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict carbon report estimate is in its life cycle. */
enum class StrictCarbonReportEstimateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a strict carbon report estimate and its current status. */
data class StrictCarbonReportEstimate(
    var code: String,
    var label: String,
    var source: String,
    var fuelLiters: Double,
    var graceDays: Int,
    var segment: String,
    var status: StrictCarbonReportEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCarbonReportEstimateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCarbonReportEstimateStatus): Boolean {
        when (status) {
            StrictCarbonReportEstimateStatus.PLANNED -> return next == StrictCarbonReportEstimateStatus.IN_PROGRESS
            StrictCarbonReportEstimateStatus.SCHEDULED -> return next == StrictCarbonReportEstimateStatus.COMPLETED
            StrictCarbonReportEstimateStatus.IN_PROGRESS -> {
                when (next) {
                    StrictCarbonReportEstimateStatus.COMPLETED, StrictCarbonReportEstimateStatus.CANCELLED -> return true
                }
                return false
            }
            StrictCarbonReportEstimateStatus.COMPLETED -> {
                when (next) {
                    StrictCarbonReportEstimateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictCarbonReportEstimateStatus) {
        if (!canMoveTo(next)) {
            error("strict carbon report estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict carbon report estimate"} $code"

    fun effectiveValue(): Double = round((graceDays.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): StrictCarbonReportEstimate {
        return StrictCarbonReportEstimate(
            code = code,
            label = label,
            source = source,
            fuelLiters = fuelLiters,
            graceDays = graceDays,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a strict carbon report estimate in status planned. */
fun newStrictCarbonReportEstimate(code: String, label: String): StrictCarbonReportEstimate {
    return StrictCarbonReportEstimate(
        code = code,
        label = label,
        source = "",
        fuelLiters = 0.0,
        graceDays = 0,
        segment = "",
        status = StrictCarbonReportEstimateStatus.PLANNED,
    )
}
