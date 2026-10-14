package com.acme.transport.fleetops.inspection.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection metric is in its life cycle. */
enum class InspectionMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One inspection metric, with what the planners need to act on it. */
data class InspectionMetric(
    var code: String,
    var name: String,
    var notes: String,
    var carrier: String,
    var distanceKm: Double,
    var maximum: Double,
    var trailerId: String,
    var pinned: Boolean,
    var revision: Int,
    var status: InspectionMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionMetricStatus): Boolean {
        when (status) {
            InspectionMetricStatus.PLANNED -> return next == InspectionMetricStatus.IN_PROGRESS || next == InspectionMetricStatus.CANCELLED
            InspectionMetricStatus.SCHEDULED -> {
                when (next) {
                    InspectionMetricStatus.IN_PROGRESS, InspectionMetricStatus.CANCELLED -> return true
                }
                return false
            }
            InspectionMetricStatus.IN_PROGRESS -> {
                when (next) {
                    InspectionMetricStatus.COMPLETED -> return true
                }
                return false
            }
            InspectionMetricStatus.COMPLETED -> return next == InspectionMetricStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: InspectionMetricStatus) {
        if (!canMoveTo(next)) {
            error("inspection metric $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun effectiveTotal(): Double = round((revision.toDouble() + maximum) * 100) / 100

    fun snapshot(): InspectionMetric {
        return InspectionMetric(
            code = code,
            name = name,
            notes = notes,
            carrier = carrier,
            distanceKm = distanceKm,
            maximum = maximum,
            trailerId = trailerId,
            pinned = pinned,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a inspection metric in status planned. */
fun newInspectionMetric(code: String, name: String): InspectionMetric {
    return InspectionMetric(
        code = code,
        name = name,
        notes = "",
        carrier = "",
        distanceKm = 0.0,
        maximum = 0.0,
        trailerId = "",
        pinned = false,
        revision = 0,
        status = InspectionMetricStatus.PLANNED,
    )
}
