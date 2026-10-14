package com.acme.transport.fleetops.serviceplan.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service plan tier records of fleetops's service plan work, and the rules, stores, and calculations
// around them.

/** Where a service plan tier is in its life cycle. */
enum class ServicePlanTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A service plan tier as the service plan team keeps it. */
data class ServicePlanTier(
    var code: String,
    var name: String,
    var margin: Double,
    var distanceKm: Double,
    var routeCode: String,
    var status: ServicePlanTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanTierStatus): Boolean {
        when (status) {
            ServicePlanTierStatus.PROPOSED -> return next == ServicePlanTierStatus.REJECTED || next == ServicePlanTierStatus.WITHDRAWN
            ServicePlanTierStatus.APPROVED -> {
                when (next) {
                    ServicePlanTierStatus.WITHDRAWN -> return true
                }
                return false
            }
            ServicePlanTierStatus.REJECTED -> return next == ServicePlanTierStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ServicePlanTierStatus) {
        if (!canMoveTo(next)) {
            error("service plan tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round((distanceKm - margin) * 100) / 100

    fun snapshot(): ServicePlanTier {
        return ServicePlanTier(
            code = code,
            name = name,
            margin = margin,
            distanceKm = distanceKm,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a service plan tier in status proposed. */
fun newServicePlanTier(code: String, name: String): ServicePlanTier {
    return ServicePlanTier(
        code = code,
        name = name,
        margin = 0.0,
        distanceKm = 0.0,
        routeCode = "",
        status = ServicePlanTierStatus.PROPOSED,
    )
}
