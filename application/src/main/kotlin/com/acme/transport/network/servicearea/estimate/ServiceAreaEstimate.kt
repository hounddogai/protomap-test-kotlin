package com.acme.transport.network.servicearea.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service area estimate records of network's service area work, and the rules, stores, and calculations
// around them.

/** Where a service area estimate is in its life cycle. */
enum class ServiceAreaEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One service area estimate, with what the planners need to act on it. */
data class ServiceAreaEstimate(
    var code: String,
    var label: String,
    var budget: Double,
    var stopCount: Int,
    var score: Double,
    var routeCode: String,
    var odometer: Int,
    var reviewer: String,
    var fuelLiters: Double,
    var status: ServiceAreaEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaEstimateStatus): Boolean {
        when (status) {
            ServiceAreaEstimateStatus.CREATED -> return next == ServiceAreaEstimateStatus.VALIDATED
            ServiceAreaEstimateStatus.VALIDATED -> return next == ServiceAreaEstimateStatus.PUBLISHED
            ServiceAreaEstimateStatus.PUBLISHED -> {
                when (next) {
                    ServiceAreaEstimateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaEstimateStatus) {
        if (!canMoveTo(next)) {
            error("service area estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedVolume(): Double = round((odometer.toDouble() + score) * 100) / 100

    fun snapshot(): ServiceAreaEstimate {
        return ServiceAreaEstimate(
            code = code,
            label = label,
            budget = budget,
            stopCount = stopCount,
            score = score,
            routeCode = routeCode,
            odometer = odometer,
            reviewer = reviewer,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a service area estimate in status created. */
fun newServiceAreaEstimate(code: String, label: String): ServiceAreaEstimate {
    return ServiceAreaEstimate(
        code = code,
        label = label,
        budget = 0.0,
        stopCount = 0,
        score = 0.0,
        routeCode = "",
        odometer = 0,
        reviewer = "",
        fuelLiters = 0.0,
        status = ServiceAreaEstimateStatus.CREATED,
    )
}
