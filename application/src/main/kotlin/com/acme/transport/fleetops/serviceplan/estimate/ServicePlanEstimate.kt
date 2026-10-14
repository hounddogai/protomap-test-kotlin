package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service plan estimate records of fleetops's service plan work, and the rules, stores, and calculations
// around them.

/** Where a service plan estimate is in its life cycle. */
enum class ServicePlanEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One service plan estimate, with what the planners need to act on it. */
data class ServicePlanEstimate(
    var code: String,
    var title: String,
    var vehicleId: String,
    var limit: Int,
    var retries: Int,
    var pinned: Boolean,
    var status: ServicePlanEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanEstimateStatus): Boolean {
        when (status) {
            ServicePlanEstimateStatus.CREATED -> return next == ServicePlanEstimateStatus.PUBLISHED || next == ServicePlanEstimateStatus.RETIRED
            ServicePlanEstimateStatus.VALIDATED -> {
                when (next) {
                    ServicePlanEstimateStatus.PUBLISHED -> return true
                }
                return false
            }
            ServicePlanEstimateStatus.PUBLISHED -> {
                when (next) {
                    ServicePlanEstimateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanEstimateStatus) {
        if (!canMoveTo(next)) {
            error("service plan estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveLoad(): Double = round((limit.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): ServicePlanEstimate {
        return ServicePlanEstimate(
            code = code,
            title = title,
            vehicleId = vehicleId,
            limit = limit,
            retries = retries,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a service plan estimate in status created. */
fun newServicePlanEstimate(code: String, title: String): ServicePlanEstimate {
    return ServicePlanEstimate(
        code = code,
        title = title,
        vehicleId = "",
        limit = 0,
        retries = 0,
        pinned = false,
        status = ServicePlanEstimateStatus.CREATED,
    )
}
