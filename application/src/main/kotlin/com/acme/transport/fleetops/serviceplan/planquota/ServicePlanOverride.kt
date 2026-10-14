package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan override is in its life cycle. */
enum class ServicePlanOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One service plan override, with what the planners need to act on it. */
data class ServicePlanOverride(
    var code: String,
    var title: String,
    var budget: Double,
    var etaMinutes: Int,
    var sequence: Int,
    var loadKg: Double,
    var routeCode: String,
    var depot: String,
    var fuelLiters: Double,
    var status: ServicePlanOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanOverrideStatus): Boolean {
        when (status) {
            ServicePlanOverrideStatus.DRAFT -> return next == ServicePlanOverrideStatus.ACTIVE || next == ServicePlanOverrideStatus.SUSPENDED
            ServicePlanOverrideStatus.PENDING -> {
                when (next) {
                    ServicePlanOverrideStatus.ACTIVE, ServicePlanOverrideStatus.CLOSED -> return true
                }
                return false
            }
            ServicePlanOverrideStatus.ACTIVE -> return next == ServicePlanOverrideStatus.SUSPENDED || next == ServicePlanOverrideStatus.CLOSED
            ServicePlanOverrideStatus.SUSPENDED -> {
                when (next) {
                    ServicePlanOverrideStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanOverrideStatus) {
        if (!canMoveTo(next)) {
            error("service plan override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedExposure(): Double = round((fuelLiters - budget) * 100) / 100

    fun snapshot(): ServicePlanOverride {
        return ServicePlanOverride(
            code = code,
            title = title,
            budget = budget,
            etaMinutes = etaMinutes,
            sequence = sequence,
            loadKg = loadKg,
            routeCode = routeCode,
            depot = depot,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a service plan override in status draft. */
fun newServicePlanOverride(code: String, title: String): ServicePlanOverride {
    return ServicePlanOverride(
        code = code,
        title = title,
        budget = 0.0,
        etaMinutes = 0,
        sequence = 0,
        loadKg = 0.0,
        routeCode = "",
        depot = "",
        fuelLiters = 0.0,
        status = ServicePlanOverrideStatus.DRAFT,
    )
}
