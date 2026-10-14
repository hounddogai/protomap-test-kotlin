package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset cycle is in its life cycle. */
enum class RouteOffsetCycleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One route offset cycle, with what the planners need to act on it. */
data class RouteOffsetCycle(
    var code: String,
    var name: String,
    var quantity: Int,
    var discount: Double,
    var routeCode: String,
    var floorValue: Double,
    var revision: Int,
    var minimum: Double,
    var status: RouteOffsetCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetCycleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetCycleStatus): Boolean {
        when (status) {
            RouteOffsetCycleStatus.CREATED -> return next == RouteOffsetCycleStatus.VALIDATED || next == RouteOffsetCycleStatus.RETIRED
            RouteOffsetCycleStatus.VALIDATED -> {
                when (next) {
                    RouteOffsetCycleStatus.RETIRED -> return true
                }
                return false
            }
            RouteOffsetCycleStatus.PUBLISHED -> return next == RouteOffsetCycleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RouteOffsetCycleStatus) {
        if (!canMoveTo(next)) {
            error("route offset cycle $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun weightedValue(): Double = round(revision.toDouble() * discount * 100) / 100

    fun snapshot(): RouteOffsetCycle {
        return RouteOffsetCycle(
            code = code,
            name = name,
            quantity = quantity,
            discount = discount,
            routeCode = routeCode,
            floorValue = floorValue,
            revision = revision,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a route offset cycle in status created. */
fun newRouteOffsetCycle(code: String, name: String): RouteOffsetCycle {
    return RouteOffsetCycle(
        code = code,
        name = name,
        quantity = 0,
        discount = 0.0,
        routeCode = "",
        floorValue = 0.0,
        revision = 0,
        minimum = 0.0,
        status = RouteOffsetCycleStatus.CREATED,
    )
}
