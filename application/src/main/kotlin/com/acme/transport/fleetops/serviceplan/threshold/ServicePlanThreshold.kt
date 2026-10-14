package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service plan threshold records of fleetops's service plan work, and the rules, stores, and calculations
// around them.

/** Where a service plan threshold is in its life cycle. */
enum class ServicePlanThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A service plan threshold as the service plan team keeps it. */
data class ServicePlanThreshold(
    var code: String,
    var title: String,
    var summary: String,
    var driverId: String,
    var ratio: Double,
    var routeCode: String,
    var floorValue: Double,
    var rate: Double,
    var status: ServicePlanThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanThresholdStatus): Boolean {
        when (status) {
            ServicePlanThresholdStatus.REQUESTED -> return next == ServicePlanThresholdStatus.REVOKED || next == ServicePlanThresholdStatus.EXPIRED
            ServicePlanThresholdStatus.GRANTED -> return next == ServicePlanThresholdStatus.REVOKED || next == ServicePlanThresholdStatus.EXPIRED
            ServicePlanThresholdStatus.REVOKED -> {
                when (next) {
                    ServicePlanThresholdStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanThresholdStatus) {
        if (!canMoveTo(next)) {
            error("service plan threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"service plan threshold"} $code"

    fun projectedValue(): Double = round((ratio + rate) * 100) / 100

    fun snapshot(): ServicePlanThreshold {
        return ServicePlanThreshold(
            code = code,
            title = title,
            summary = summary,
            driverId = driverId,
            ratio = ratio,
            routeCode = routeCode,
            floorValue = floorValue,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a service plan threshold in status requested. */
fun newServicePlanThreshold(code: String, title: String): ServicePlanThreshold {
    return ServicePlanThreshold(
        code = code,
        title = title,
        summary = "",
        driverId = "",
        ratio = 0.0,
        routeCode = "",
        floorValue = 0.0,
        rate = 0.0,
        status = ServicePlanThresholdStatus.REQUESTED,
    )
}
