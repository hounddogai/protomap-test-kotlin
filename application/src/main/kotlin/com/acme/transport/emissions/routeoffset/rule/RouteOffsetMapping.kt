package com.acme.transport.emissions.routeoffset.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset mapping is in its life cycle. */
enum class RouteOffsetMappingStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A route offset mapping. Its code is unique within a tenant. */
data class RouteOffsetMapping(
    var code: String,
    var title: String,
    var priority: Int,
    var fuelLiters: Double,
    var odometer: Int,
    var threshold: Double,
    var status: RouteOffsetMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetMappingStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetMappingStatus): Boolean {
        when (status) {
            RouteOffsetMappingStatus.DRAFT -> {
                when (next) {
                    RouteOffsetMappingStatus.SUSPENDED -> return true
                }
                return false
            }
            RouteOffsetMappingStatus.PENDING -> {
                when (next) {
                    RouteOffsetMappingStatus.ACTIVE -> return true
                }
                return false
            }
            RouteOffsetMappingStatus.ACTIVE -> return next == RouteOffsetMappingStatus.SUSPENDED
            RouteOffsetMappingStatus.SUSPENDED -> return next == RouteOffsetMappingStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RouteOffsetMappingStatus) {
        if (!canMoveTo(next)) {
            error("route offset mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"route offset mapping"} $code"

    fun effectiveExposure(): Double = round((fuelLiters - threshold) * 100) / 100

    fun snapshot(): RouteOffsetMapping {
        return RouteOffsetMapping(
            code = code,
            title = title,
            priority = priority,
            fuelLiters = fuelLiters,
            odometer = odometer,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a route offset mapping in status draft. */
fun newRouteOffsetMapping(code: String, title: String): RouteOffsetMapping {
    return RouteOffsetMapping(
        code = code,
        title = title,
        priority = 0,
        fuelLiters = 0.0,
        odometer = 0,
        threshold = 0.0,
        status = RouteOffsetMappingStatus.DRAFT,
    )
}
