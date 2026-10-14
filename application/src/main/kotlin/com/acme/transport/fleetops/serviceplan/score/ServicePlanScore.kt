package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service plan score records of fleetops's service plan work, and the rules, stores, and calculations
// around them.

/** Where a service plan score is in its life cycle. */
enum class ServicePlanScoreStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One service plan score, with what the planners need to act on it. */
data class ServicePlanScore(
    var code: String,
    var name: String,
    var segment: String,
    var currency: String,
    var routeCode: String,
    var weight: Double,
    var floorValue: Double,
    var status: ServicePlanScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanScoreStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanScoreStatus): Boolean {
        when (status) {
            ServicePlanScoreStatus.IDLE -> {
                when (next) {
                    ServicePlanScoreStatus.READY -> return true
                }
                return false
            }
            ServicePlanScoreStatus.WARMING -> return next == ServicePlanScoreStatus.STOPPED
            ServicePlanScoreStatus.READY -> return next == ServicePlanScoreStatus.STOPPED
            ServicePlanScoreStatus.DRAINING -> {
                when (next) {
                    ServicePlanScoreStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanScoreStatus) {
        if (!canMoveTo(next)) {
            error("service plan score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"service plan score"} $code"

    fun effectiveExposure(): Double = round((weight - floorValue) * 100) / 100

    fun snapshot(): ServicePlanScore {
        return ServicePlanScore(
            code = code,
            name = name,
            segment = segment,
            currency = currency,
            routeCode = routeCode,
            weight = weight,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a service plan score in status idle. */
fun newServicePlanScore(code: String, name: String): ServicePlanScore {
    return ServicePlanScore(
        code = code,
        name = name,
        segment = "",
        currency = "",
        routeCode = "",
        weight = 0.0,
        floorValue = 0.0,
        status = ServicePlanScoreStatus.IDLE,
    )
}
