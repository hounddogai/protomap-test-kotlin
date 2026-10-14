package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset target is in its life cycle. */
enum class RouteOffsetTargetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A route offset target as the route offset team keeps it. */
data class RouteOffsetTarget(
    var code: String,
    var title: String,
    var budget: Double,
    var source: String,
    var score: Double,
    var vehicleId: String,
    var segment: String,
    var active: Boolean,
    var category: String,
    var status: RouteOffsetTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetTargetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetTargetStatus): Boolean {
        when (status) {
            RouteOffsetTargetStatus.DRAFT -> {
                when (next) {
                    RouteOffsetTargetStatus.CLOSED -> return true
                }
                return false
            }
            RouteOffsetTargetStatus.PENDING -> {
                when (next) {
                    RouteOffsetTargetStatus.ACTIVE -> return true
                }
                return false
            }
            RouteOffsetTargetStatus.ACTIVE -> {
                when (next) {
                    RouteOffsetTargetStatus.CLOSED -> return true
                }
                return false
            }
            RouteOffsetTargetStatus.SUSPENDED -> return next == RouteOffsetTargetStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RouteOffsetTargetStatus) {
        if (!canMoveTo(next)) {
            error("route offset target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"route offset target"} $code"

    fun netVolume(): Double = round(score * budget * 100) / 100

    fun snapshot(): RouteOffsetTarget {
        return RouteOffsetTarget(
            code = code,
            title = title,
            budget = budget,
            source = source,
            score = score,
            vehicleId = vehicleId,
            segment = segment,
            active = active,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a route offset target in status draft. */
fun newRouteOffsetTarget(code: String, title: String): RouteOffsetTarget {
    return RouteOffsetTarget(
        code = code,
        title = title,
        budget = 0.0,
        source = "",
        score = 0.0,
        vehicleId = "",
        segment = "",
        active = false,
        category = "",
        status = RouteOffsetTargetStatus.DRAFT,
    )
}
