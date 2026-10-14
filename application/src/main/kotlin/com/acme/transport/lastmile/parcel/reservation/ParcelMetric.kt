package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel metric is in its life cycle. */
enum class ParcelMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a parcel metric and its current status. */
data class ParcelMetric(
    var code: String,
    var label: String,
    var cost: Double,
    var budget: Double,
    var minimum: Double,
    var summary: String,
    var floorValue: Double,
    var status: ParcelMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelMetricStatus): Boolean {
        when (status) {
            ParcelMetricStatus.REQUESTED -> return next == ParcelMetricStatus.EXPIRED
            ParcelMetricStatus.GRANTED -> {
                when (next) {
                    ParcelMetricStatus.REVOKED, ParcelMetricStatus.EXPIRED -> return true
                }
                return false
            }
            ParcelMetricStatus.REVOKED -> {
                when (next) {
                    ParcelMetricStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelMetricStatus) {
        if (!canMoveTo(next)) {
            error("parcel metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedValue(): Double = round(floorValue * cost * 100) / 100

    fun snapshot(): ParcelMetric {
        return ParcelMetric(
            code = code,
            label = label,
            cost = cost,
            budget = budget,
            minimum = minimum,
            summary = summary,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a parcel metric in status requested. */
fun newParcelMetric(code: String, label: String): ParcelMetric {
    return ParcelMetric(
        code = code,
        label = label,
        cost = 0.0,
        budget = 0.0,
        minimum = 0.0,
        summary = "",
        floorValue = 0.0,
        status = ParcelMetricStatus.REQUESTED,
    )
}
