package com.acme.transport.fleetops.serviceplan.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service plan rollup records of fleetops's service plan work, and the rules, stores, and calculations
// around them.

/** Where a service plan rollup is in its life cycle. */
enum class ServicePlanRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A service plan rollup as the service plan team keeps it. */
data class ServicePlanRollup(
    var code: String,
    var name: String,
    var shiftCode: String,
    var vehicleId: String,
    var attributes: MutableMap<String, String>,
    var sequence: Int,
    var distanceKm: Double,
    var externalRef: String,
    var threshold: Double,
    var pinned: Boolean,
    var depot: String,
    var status: ServicePlanRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanRollupStatus): Boolean {
        when (status) {
            ServicePlanRollupStatus.CREATED -> {
                when (next) {
                    ServicePlanRollupStatus.VALIDATED, ServicePlanRollupStatus.PUBLISHED -> return true
                }
                return false
            }
            ServicePlanRollupStatus.VALIDATED -> return next == ServicePlanRollupStatus.RETIRED
            ServicePlanRollupStatus.PUBLISHED -> {
                when (next) {
                    ServicePlanRollupStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanRollupStatus) {
        if (!canMoveTo(next)) {
            error("service plan rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round(distanceKm * threshold * 100) / 100

    fun snapshot(): ServicePlanRollup {
        return ServicePlanRollup(
            code = code,
            name = name,
            shiftCode = shiftCode,
            vehicleId = vehicleId,
            attributes = attributes,
            sequence = sequence,
            distanceKm = distanceKm,
            externalRef = externalRef,
            threshold = threshold,
            pinned = pinned,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a service plan rollup in status created. */
fun newServicePlanRollup(code: String, name: String): ServicePlanRollup {
    return ServicePlanRollup(
        code = code,
        name = name,
        shiftCode = "",
        vehicleId = "",
        attributes = mutableMapOf<String, String>(),
        sequence = 0,
        distanceKm = 0.0,
        externalRef = "",
        threshold = 0.0,
        pinned = false,
        depot = "",
        status = ServicePlanRollupStatus.CREATED,
    )
}
