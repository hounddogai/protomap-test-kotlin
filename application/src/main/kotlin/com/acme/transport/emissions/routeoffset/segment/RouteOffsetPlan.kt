package com.acme.transport.emissions.routeoffset.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset plan is in its life cycle. */
enum class RouteOffsetPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One route offset plan, with what the planners need to act on it. */
data class RouteOffsetPlan(
    var code: String,
    var name: String,
    var odometer: Int,
    var sequence: Int,
    var createdAt: Int,
    var channel: String,
    var rate: Double,
    var routeCode: String,
    var externalRef: String,
    var status: RouteOffsetPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetPlanStatus): Boolean {
        when (status) {
            RouteOffsetPlanStatus.OPEN -> {
                when (next) {
                    RouteOffsetPlanStatus.RESOLVED, RouteOffsetPlanStatus.ARCHIVED -> return true
                }
                return false
            }
            RouteOffsetPlanStatus.IN_REVIEW -> {
                when (next) {
                    RouteOffsetPlanStatus.REOPENED, RouteOffsetPlanStatus.ARCHIVED -> return true
                }
                return false
            }
            RouteOffsetPlanStatus.RESOLVED -> {
                when (next) {
                    RouteOffsetPlanStatus.REOPENED, RouteOffsetPlanStatus.ARCHIVED -> return true
                }
                return false
            }
            RouteOffsetPlanStatus.REOPENED -> {
                when (next) {
                    RouteOffsetPlanStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetPlanStatus) {
        if (!canMoveTo(next)) {
            error("route offset plan $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"route offset plan"} $code"

    fun adjustedExposure(): Double = round((rate - odometer.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetPlan {
        return RouteOffsetPlan(
            code = code,
            name = name,
            odometer = odometer,
            sequence = sequence,
            createdAt = createdAt,
            channel = channel,
            rate = rate,
            routeCode = routeCode,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a route offset plan in status open. */
fun newRouteOffsetPlan(code: String, name: String): RouteOffsetPlan {
    return RouteOffsetPlan(
        code = code,
        name = name,
        odometer = 0,
        sequence = 0,
        createdAt = 0,
        channel = "",
        rate = 0.0,
        routeCode = "",
        externalRef = "",
        status = RouteOffsetPlanStatus.OPEN,
    )
}
