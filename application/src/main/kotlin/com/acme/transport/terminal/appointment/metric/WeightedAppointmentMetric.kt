package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted appointment metric records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a weighted appointment metric is in its life cycle. */
enum class WeightedAppointmentMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted appointment metric. Its code is unique within a tenant. */
data class WeightedAppointmentMetric(
    var code: String,
    var name: String,
    var floorValue: Double,
    var graceDays: Int,
    var externalRef: String,
    var margin: Double,
    var batchSize: Int,
    var region: String,
    var status: WeightedAppointmentMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAppointmentMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAppointmentMetricStatus): Boolean {
        when (status) {
            WeightedAppointmentMetricStatus.REQUESTED -> {
                when (next) {
                    WeightedAppointmentMetricStatus.REVOKED, WeightedAppointmentMetricStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedAppointmentMetricStatus.GRANTED -> {
                when (next) {
                    WeightedAppointmentMetricStatus.REVOKED -> return true
                }
                return false
            }
            WeightedAppointmentMetricStatus.REVOKED -> return next == WeightedAppointmentMetricStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: WeightedAppointmentMetricStatus) {
        if (!canMoveTo(next)) {
            error("weighted appointment metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted appointment metric"} $code"

    fun netVolume(): Double = round((graceDays.toDouble() - floorValue) * 100) / 100

    fun snapshot(): WeightedAppointmentMetric {
        return WeightedAppointmentMetric(
            code = code,
            name = name,
            floorValue = floorValue,
            graceDays = graceDays,
            externalRef = externalRef,
            margin = margin,
            batchSize = batchSize,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a weighted appointment metric in status requested. */
fun newWeightedAppointmentMetric(code: String, name: String): WeightedAppointmentMetric {
    return WeightedAppointmentMetric(
        code = code,
        name = name,
        floorValue = 0.0,
        graceDays = 0,
        externalRef = "",
        margin = 0.0,
        batchSize = 0,
        region = "",
        status = WeightedAppointmentMetricStatus.REQUESTED,
    )
}
