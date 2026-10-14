package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset tier is in its life cycle. */
enum class RouteOffsetTierStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a route offset tier and its current status. */
data class RouteOffsetTier(
    var code: String,
    var name: String,
    var rate: Double,
    var endsAt: Int,
    var category: String,
    var maximum: Double,
    var status: RouteOffsetTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetTierStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetTierStatus): Boolean {
        when (status) {
            RouteOffsetTierStatus.IDLE -> return next == RouteOffsetTierStatus.DRAINING || next == RouteOffsetTierStatus.STOPPED
            RouteOffsetTierStatus.WARMING -> {
                when (next) {
                    RouteOffsetTierStatus.DRAINING -> return true
                }
                return false
            }
            RouteOffsetTierStatus.READY -> {
                when (next) {
                    RouteOffsetTierStatus.STOPPED -> return true
                }
                return false
            }
            RouteOffsetTierStatus.DRAINING -> {
                when (next) {
                    RouteOffsetTierStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetTierStatus) {
        if (!canMoveTo(next)) {
            error("route offset tier $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"route offset tier"} $code"

    fun netLoad(): Double = round(maximum * rate * 100) / 100

    fun snapshot(): RouteOffsetTier {
        return RouteOffsetTier(
            code = code,
            name = name,
            rate = rate,
            endsAt = endsAt,
            category = category,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a route offset tier in status idle. */
fun newRouteOffsetTier(code: String, name: String): RouteOffsetTier {
    return RouteOffsetTier(
        code = code,
        name = name,
        rate = 0.0,
        endsAt = 0,
        category = "",
        maximum = 0.0,
        status = RouteOffsetTierStatus.IDLE,
    )
}
