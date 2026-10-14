package com.acme.transport.emissions.emissionfactor.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled emission factor metric is in its life cycle. */
enum class ScheduledEmissionFactorMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled emission factor metric as the emission factor team keeps it. */
data class ScheduledEmissionFactorMetric(
    var code: String,
    var label: String,
    var carrier: String,
    var externalRef: String,
    var batchSize: Int,
    var distanceKm: Double,
    var unit: String,
    var attempts: Int,
    var odometer: Int,
    var status: ScheduledEmissionFactorMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledEmissionFactorMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledEmissionFactorMetricStatus): Boolean {
        when (status) {
            ScheduledEmissionFactorMetricStatus.OPEN -> {
                when (next) {
                    ScheduledEmissionFactorMetricStatus.RESOLVED, ScheduledEmissionFactorMetricStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledEmissionFactorMetricStatus.IN_REVIEW -> return next == ScheduledEmissionFactorMetricStatus.RESOLVED || next == ScheduledEmissionFactorMetricStatus.REOPENED
            ScheduledEmissionFactorMetricStatus.RESOLVED -> {
                when (next) {
                    ScheduledEmissionFactorMetricStatus.REOPENED, ScheduledEmissionFactorMetricStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledEmissionFactorMetricStatus.REOPENED -> {
                when (next) {
                    ScheduledEmissionFactorMetricStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledEmissionFactorMetricStatus) {
        if (!canMoveTo(next)) {
            error("scheduled emission factor metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedVolume(): Double = round((distanceKm + attempts.toDouble()) * 100) / 100

    fun snapshot(): ScheduledEmissionFactorMetric {
        return ScheduledEmissionFactorMetric(
            code = code,
            label = label,
            carrier = carrier,
            externalRef = externalRef,
            batchSize = batchSize,
            distanceKm = distanceKm,
            unit = unit,
            attempts = attempts,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled emission factor metric in status open. */
fun newScheduledEmissionFactorMetric(code: String, label: String): ScheduledEmissionFactorMetric {
    return ScheduledEmissionFactorMetric(
        code = code,
        label = label,
        carrier = "",
        externalRef = "",
        batchSize = 0,
        distanceKm = 0.0,
        unit = "",
        attempts = 0,
        odometer = 0,
        status = ScheduledEmissionFactorMetricStatus.OPEN,
    )
}
