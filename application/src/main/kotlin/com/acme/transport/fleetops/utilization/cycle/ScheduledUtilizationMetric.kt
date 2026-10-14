package com.acme.transport.fleetops.utilization.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled utilization metric is in its life cycle. */
enum class ScheduledUtilizationMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a scheduled utilization metric and its current status. */
data class ScheduledUtilizationMetric(
    var code: String,
    var label: String,
    var maximum: Double,
    var stopCount: Int,
    var quantity: Int,
    var status: ScheduledUtilizationMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledUtilizationMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledUtilizationMetricStatus): Boolean {
        when (status) {
            ScheduledUtilizationMetricStatus.PLANNED -> {
                when (next) {
                    ScheduledUtilizationMetricStatus.SCHEDULED -> return true
                }
                return false
            }
            ScheduledUtilizationMetricStatus.SCHEDULED -> return next == ScheduledUtilizationMetricStatus.COMPLETED
            ScheduledUtilizationMetricStatus.IN_PROGRESS -> return next == ScheduledUtilizationMetricStatus.COMPLETED
            ScheduledUtilizationMetricStatus.COMPLETED -> return next == ScheduledUtilizationMetricStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ScheduledUtilizationMetricStatus) {
        if (!canMoveTo(next)) {
            error("scheduled utilization metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled utilization metric"} $code"

    fun projectedLoad(): Double = round(quantity.toDouble() * maximum * 100) / 100

    fun snapshot(): ScheduledUtilizationMetric {
        return ScheduledUtilizationMetric(
            code = code,
            label = label,
            maximum = maximum,
            stopCount = stopCount,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled utilization metric in status planned. */
fun newScheduledUtilizationMetric(code: String, label: String): ScheduledUtilizationMetric {
    return ScheduledUtilizationMetric(
        code = code,
        label = label,
        maximum = 0.0,
        stopCount = 0,
        quantity = 0,
        status = ScheduledUtilizationMetricStatus.PLANNED,
    )
}
