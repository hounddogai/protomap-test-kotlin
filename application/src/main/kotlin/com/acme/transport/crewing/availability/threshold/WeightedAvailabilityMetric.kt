package com.acme.transport.crewing.availability.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted availability metric is in its life cycle. */
enum class WeightedAvailabilityMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted availability metric. Its code is unique within a tenant. */
data class WeightedAvailabilityMetric(
    var code: String,
    var label: String,
    var vehicleId: String,
    var durationMinutes: Int,
    var checksum: String,
    var externalRef: String,
    var updatedAt: Int,
    var locale: String,
    var status: WeightedAvailabilityMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAvailabilityMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAvailabilityMetricStatus): Boolean {
        when (status) {
            WeightedAvailabilityMetricStatus.PLANNED -> {
                when (next) {
                    WeightedAvailabilityMetricStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedAvailabilityMetricStatus.SCHEDULED -> return next == WeightedAvailabilityMetricStatus.COMPLETED
            WeightedAvailabilityMetricStatus.IN_PROGRESS -> return next == WeightedAvailabilityMetricStatus.COMPLETED || next == WeightedAvailabilityMetricStatus.CANCELLED
            WeightedAvailabilityMetricStatus.COMPLETED -> {
                when (next) {
                    WeightedAvailabilityMetricStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedAvailabilityMetricStatus) {
        if (!canMoveTo(next)) {
            error("weighted availability metric $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedLoad(): Double = round((durationMinutes.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedAvailabilityMetric {
        return WeightedAvailabilityMetric(
            code = code,
            label = label,
            vehicleId = vehicleId,
            durationMinutes = durationMinutes,
            checksum = checksum,
            externalRef = externalRef,
            updatedAt = updatedAt,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a weighted availability metric in status planned. */
fun newWeightedAvailabilityMetric(code: String, label: String): WeightedAvailabilityMetric {
    return WeightedAvailabilityMetric(
        code = code,
        label = label,
        vehicleId = "",
        durationMinutes = 0,
        checksum = "",
        externalRef = "",
        updatedAt = 0,
        locale = "",
        status = WeightedAvailabilityMetricStatus.PLANNED,
    )
}
