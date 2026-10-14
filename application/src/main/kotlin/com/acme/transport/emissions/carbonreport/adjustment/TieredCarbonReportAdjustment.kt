package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered carbon report adjustment is in its life cycle. */
enum class TieredCarbonReportAdjustmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a tiered carbon report adjustment and its current status. */
data class TieredCarbonReportAdjustment(
    var code: String,
    var label: String,
    var rate: Double,
    var odometer: Int,
    var floorValue: Double,
    var dock: String,
    var category: String,
    var trailerId: String,
    var maximum: Double,
    var approved: Boolean,
    var status: TieredCarbonReportAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCarbonReportAdjustmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCarbonReportAdjustmentStatus): Boolean {
        when (status) {
            TieredCarbonReportAdjustmentStatus.PLANNED -> return next == TieredCarbonReportAdjustmentStatus.COMPLETED
            TieredCarbonReportAdjustmentStatus.SCHEDULED -> {
                when (next) {
                    TieredCarbonReportAdjustmentStatus.COMPLETED, TieredCarbonReportAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            TieredCarbonReportAdjustmentStatus.IN_PROGRESS -> {
                when (next) {
                    TieredCarbonReportAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            TieredCarbonReportAdjustmentStatus.COMPLETED -> return next == TieredCarbonReportAdjustmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredCarbonReportAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("tiered carbon report adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedVolume(): Double = round((rate + odometer.toDouble()) * 100) / 100

    fun snapshot(): TieredCarbonReportAdjustment {
        return TieredCarbonReportAdjustment(
            code = code,
            label = label,
            rate = rate,
            odometer = odometer,
            floorValue = floorValue,
            dock = dock,
            category = category,
            trailerId = trailerId,
            maximum = maximum,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tiered carbon report adjustment in status planned. */
fun newTieredCarbonReportAdjustment(code: String, label: String): TieredCarbonReportAdjustment {
    return TieredCarbonReportAdjustment(
        code = code,
        label = label,
        rate = 0.0,
        odometer = 0,
        floorValue = 0.0,
        dock = "",
        category = "",
        trailerId = "",
        maximum = 0.0,
        approved = false,
        status = TieredCarbonReportAdjustmentStatus.PLANNED,
    )
}
