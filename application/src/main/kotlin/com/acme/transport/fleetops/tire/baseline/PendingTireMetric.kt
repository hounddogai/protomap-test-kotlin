package com.acme.transport.fleetops.tire.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending tire metric is in its life cycle. */
enum class PendingTireMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending tire metric. Its code is unique within a tenant. */
data class PendingTireMetric(
    var code: String,
    var name: String,
    var trailerId: String,
    var capacity: Int,
    var description: String,
    var durationMinutes: Int,
    var odometer: Int,
    var ceiling: Double,
    var status: PendingTireMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTireMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTireMetricStatus): Boolean {
        when (status) {
            PendingTireMetricStatus.PLANNED -> return next == PendingTireMetricStatus.IN_PROGRESS
            PendingTireMetricStatus.SCHEDULED -> {
                when (next) {
                    PendingTireMetricStatus.COMPLETED, PendingTireMetricStatus.CANCELLED -> return true
                }
                return false
            }
            PendingTireMetricStatus.IN_PROGRESS -> {
                when (next) {
                    PendingTireMetricStatus.CANCELLED -> return true
                }
                return false
            }
            PendingTireMetricStatus.COMPLETED -> {
                when (next) {
                    PendingTireMetricStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingTireMetricStatus) {
        if (!canMoveTo(next)) {
            error("pending tire metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending tire metric"} $code"

    fun effectiveLoad(): Double = round((durationMinutes.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): PendingTireMetric {
        return PendingTireMetric(
            code = code,
            name = name,
            trailerId = trailerId,
            capacity = capacity,
            description = description,
            durationMinutes = durationMinutes,
            odometer = odometer,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending tire metric in status planned. */
fun newPendingTireMetric(code: String, name: String): PendingTireMetric {
    return PendingTireMetric(
        code = code,
        name = name,
        trailerId = "",
        capacity = 0,
        description = "",
        durationMinutes = 0,
        odometer = 0,
        ceiling = 0.0,
        status = PendingTireMetricStatus.PLANNED,
    )
}
