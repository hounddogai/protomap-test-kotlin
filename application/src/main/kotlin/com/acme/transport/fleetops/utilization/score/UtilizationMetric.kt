package com.acme.transport.fleetops.utilization.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization metric is in its life cycle. */
enum class UtilizationMetricStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One utilization metric, with what the planners need to act on it. */
data class UtilizationMetric(
    var code: String,
    var name: String,
    var checksum: String,
    var stopCount: Int,
    var odometer: Int,
    var windowDays: Int,
    var country: String,
    var budget: Double,
    var title: String,
    var durationMinutes: Int,
    var description: String,
    var status: UtilizationMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationMetricStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationMetricStatus): Boolean {
        when (status) {
            UtilizationMetricStatus.CREATED -> return next == UtilizationMetricStatus.PUBLISHED || next == UtilizationMetricStatus.RETIRED
            UtilizationMetricStatus.VALIDATED -> {
                when (next) {
                    UtilizationMetricStatus.PUBLISHED -> return true
                }
                return false
            }
            UtilizationMetricStatus.PUBLISHED -> {
                when (next) {
                    UtilizationMetricStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationMetricStatus) {
        if (!canMoveTo(next)) {
            error("utilization metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedExposure(): Double = round((windowDays.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): UtilizationMetric {
        return UtilizationMetric(
            code = code,
            name = name,
            checksum = checksum,
            stopCount = stopCount,
            odometer = odometer,
            windowDays = windowDays,
            country = country,
            budget = budget,
            title = title,
            durationMinutes = durationMinutes,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a utilization metric in status created. */
fun newUtilizationMetric(code: String, name: String): UtilizationMetric {
    return UtilizationMetric(
        code = code,
        name = name,
        checksum = "",
        stopCount = 0,
        odometer = 0,
        windowDays = 0,
        country = "",
        budget = 0.0,
        title = "",
        durationMinutes = 0,
        description = "",
        status = UtilizationMetricStatus.CREATED,
    )
}
