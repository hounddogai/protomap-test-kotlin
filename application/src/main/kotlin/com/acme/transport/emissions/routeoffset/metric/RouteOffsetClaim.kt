package com.acme.transport.emissions.routeoffset.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset claim is in its life cycle. */
enum class RouteOffsetClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a route offset claim and its current status. */
data class RouteOffsetClaim(
    var code: String,
    var label: String,
    var trailerId: String,
    var createdAt: Int,
    var odometer: Int,
    var category: String,
    var routeCode: String,
    var cost: Double,
    var status: RouteOffsetClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetClaimStatus): Boolean {
        when (status) {
            RouteOffsetClaimStatus.CREATED -> return next == RouteOffsetClaimStatus.PUBLISHED || next == RouteOffsetClaimStatus.RETIRED
            RouteOffsetClaimStatus.VALIDATED -> {
                when (next) {
                    RouteOffsetClaimStatus.PUBLISHED, RouteOffsetClaimStatus.RETIRED -> return true
                }
                return false
            }
            RouteOffsetClaimStatus.PUBLISHED -> {
                when (next) {
                    RouteOffsetClaimStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetClaimStatus) {
        if (!canMoveTo(next)) {
            error("route offset claim $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netLoad(): Double = round((cost - createdAt.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetClaim {
        return RouteOffsetClaim(
            code = code,
            label = label,
            trailerId = trailerId,
            createdAt = createdAt,
            odometer = odometer,
            category = category,
            routeCode = routeCode,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a route offset claim in status created. */
fun newRouteOffsetClaim(code: String, label: String): RouteOffsetClaim {
    return RouteOffsetClaim(
        code = code,
        label = label,
        trailerId = "",
        createdAt = 0,
        odometer = 0,
        category = "",
        routeCode = "",
        cost = 0.0,
        status = RouteOffsetClaimStatus.CREATED,
    )
}
