package com.acme.transport.fleetops.serviceplan.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service plan metric records of fleetops's service plan work, and the rules, stores, and calculations
// around them.

/** Where a service plan metric is in its life cycle. */
enum class ServicePlanMetricStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A service plan metric. Its code is unique within a tenant. */
data class ServicePlanMetric(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var active: Boolean,
    var label: String,
    var floorValue: Double,
    var amount: Double,
    var category: String,
    var fuelLiters: Double,
    var reviewer: String,
    var status: ServicePlanMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanMetricStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanMetricStatus): Boolean {
        when (status) {
            ServicePlanMetricStatus.CREATED -> return next == ServicePlanMetricStatus.VALIDATED || next == ServicePlanMetricStatus.RETIRED
            ServicePlanMetricStatus.VALIDATED -> {
                when (next) {
                    ServicePlanMetricStatus.PUBLISHED, ServicePlanMetricStatus.RETIRED -> return true
                }
                return false
            }
            ServicePlanMetricStatus.PUBLISHED -> {
                when (next) {
                    ServicePlanMetricStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanMetricStatus) {
        if (!canMoveTo(next)) {
            error("service plan metric $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($title)"

    fun weightedTotal(): Double = round(floorValue * fuelLiters * 100) / 100

    fun snapshot(): ServicePlanMetric {
        return ServicePlanMetric(
            code = code,
            title = title,
            expiresAt = expiresAt,
            active = active,
            label = label,
            floorValue = floorValue,
            amount = amount,
            category = category,
            fuelLiters = fuelLiters,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a service plan metric in status created. */
fun newServicePlanMetric(code: String, title: String): ServicePlanMetric {
    return ServicePlanMetric(
        code = code,
        title = title,
        expiresAt = 0,
        active = false,
        label = "",
        floorValue = 0.0,
        amount = 0.0,
        category = "",
        fuelLiters = 0.0,
        reviewer = "",
        status = ServicePlanMetricStatus.CREATED,
    )
}
