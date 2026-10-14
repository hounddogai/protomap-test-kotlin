package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled service plan metric is in its life cycle. */
enum class ScheduledServicePlanMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a scheduled service plan metric and its current status. */
data class ScheduledServicePlanMetric(
    var code: String,
    var title: String,
    var minimum: Double,
    var country: String,
    var limit: Int,
    var trailerId: String,
    var stopCount: Int,
    var attempts: Int,
    var channel: String,
    var vehicleId: String,
    var floorValue: Double,
    var status: ScheduledServicePlanMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledServicePlanMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledServicePlanMetricStatus): Boolean {
        when (status) {
            ScheduledServicePlanMetricStatus.OPEN -> {
                when (next) {
                    ScheduledServicePlanMetricStatus.IN_REVIEW, ScheduledServicePlanMetricStatus.RESOLVED -> return true
                }
                return false
            }
            ScheduledServicePlanMetricStatus.IN_REVIEW -> return next == ScheduledServicePlanMetricStatus.REOPENED || next == ScheduledServicePlanMetricStatus.ARCHIVED
            ScheduledServicePlanMetricStatus.RESOLVED -> {
                when (next) {
                    ScheduledServicePlanMetricStatus.REOPENED, ScheduledServicePlanMetricStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledServicePlanMetricStatus.REOPENED -> {
                when (next) {
                    ScheduledServicePlanMetricStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledServicePlanMetricStatus) {
        if (!canMoveTo(next)) {
            error("scheduled service plan metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedLoad(): Double = round((floorValue + minimum) * 100) / 100

    fun snapshot(): ScheduledServicePlanMetric {
        return ScheduledServicePlanMetric(
            code = code,
            title = title,
            minimum = minimum,
            country = country,
            limit = limit,
            trailerId = trailerId,
            stopCount = stopCount,
            attempts = attempts,
            channel = channel,
            vehicleId = vehicleId,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a scheduled service plan metric in status open. */
fun newScheduledServicePlanMetric(code: String, title: String): ScheduledServicePlanMetric {
    return ScheduledServicePlanMetric(
        code = code,
        title = title,
        minimum = 0.0,
        country = "",
        limit = 0,
        trailerId = "",
        stopCount = 0,
        attempts = 0,
        channel = "",
        vehicleId = "",
        floorValue = 0.0,
        status = ScheduledServicePlanMetricStatus.OPEN,
    )
}
